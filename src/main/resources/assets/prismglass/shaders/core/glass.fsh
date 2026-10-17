#version 150

// Prism Glass: screen-space crystal glass. Independent implementation of well known,
// public techniques (rounded-box SDF, SDF-gradient normals, Schlick-style edge falloff).
//
// Sampler0 = blurred copy of the scene, Sampler1 = sharp copy of the scene.
// Mode 0 = pane, 1 = etched slot overlay (no backdrop sampling), 2 = button.
// Params: x = tint veil, y = hover, z = blur mix, w = edge refraction (GUI px).
uniform sampler2D Sampler0;
uniform sampler2D Sampler1;
uniform vec2 FbSize;
uniform vec2 HalfSize;
uniform float Radius;
uniform float Mode;
uniform float GuiScale;
uniform float Time;
uniform vec4 Params;

in vec2 local;
in vec4 vertexColor;
out vec4 fragColor;

float sdBox(vec2 p, vec2 b, float r) {
    vec2 q = abs(p) - (b - r);
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - r;
}

vec2 sdNormal(vec2 p, vec2 b, float r) {
    const float e = 0.5;
    vec2 g = vec2(
        sdBox(p + vec2(e, 0.0), b, r) - sdBox(p - vec2(e, 0.0), b, r),
        sdBox(p + vec2(0.0, e), b, r) - sdBox(p - vec2(0.0, e), b, r));
    float l = length(g);
    return l < 1e-4 ? vec2(0.0, -1.0) : g / l;
}

float hash(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

float luma(vec3 c) {
    return dot(c, vec3(0.299, 0.587, 0.114));
}

void main() {
    float aa = 1.0 / GuiScale;
    float d = sdBox(local, HalfSize, Radius);
    vec2 n = sdNormal(local, HalfSize, Radius);

    // ---- etched slot: only modulates the pane underneath ----
    if (Mode > 0.5 && Mode < 1.5) {
        float cover = 1.0 - smoothstep(-aa * 0.5, aa * 0.5, d);
        float depth = clamp(-d / 2.2, 0.0, 1.0);
        float edge = 1.0 - depth;
        float side = dot(n, normalize(vec2(-0.7, -0.7)));
        float shade = max(side, 0.0) * edge * edge;
        float lit = max(-side, 0.0) * edge * edge;
        float ad = clamp(0.15 + shade * 0.45, 0.0, 1.0);
        float al = clamp(lit * 0.55, 0.0, 1.0);
        float a = 1.0 - (1.0 - ad) * (1.0 - al);
        vec3 dark = vec3(0.03, 0.07, 0.12);
        vec3 light = mix(vec3(1.0), vertexColor.rgb, 0.4);
        vec3 rgb = (dark * ad * (1.0 - al) + light * al) / max(a, 1e-4);
        fragColor = vec4(rgb, a * cover);
        if (fragColor.a < 0.003) discard;
        return;
    }

    bool button = Mode > 1.5;
    float bevel = min(button ? 3.5 : 9.0, min(HalfSize.x, HalfSize.y));
    float cover = 1.0 - smoothstep(-aa * 0.5, aa * 0.5, d);
    float depth = clamp(-d / bevel, 0.0, 1.0);
    float edge = 1.0 - depth;
    float bulge = edge * edge;

    // ---- refraction: the rim bends the backdrop like a thick glass lip ----
    vec2 duv = (-n * bulge * Params.w) * GuiScale / FbSize;
    vec2 uv0 = gl_FragCoord.xy / FbSize;
    vec3 sharp;
    sharp.r = texture(Sampler1, uv0 + duv * 1.10).r;
    sharp.g = texture(Sampler1, uv0 + duv).g;
    sharp.b = texture(Sampler1, uv0 + duv * 0.90).b;
    vec3 soft = texture(Sampler0, uv0 + duv).rgb;
    vec3 col = mix(sharp, soft, mix(Params.z, Params.z * 0.35, bulge));

    // Legibility: keep the scene visible but calm its contrast and brightness spikes.
    col = mix(col, vec3(luma(col)), 0.22);
    vec3 tint = vertexColor.rgb;
    col = mix(col, tint, Params.x);

    // Vertical body gradient: light from above, slightly denser toward the bottom.
    float ty = clamp(local.y / max(HalfSize.y, 1.0) * 0.5 + 0.5, 0.0, 1.0);
    col += tint * (0.07 * (1.0 - ty)) - vec3(0.02, 0.03, 0.04) * ty;

    // ---- edge light ----
    vec2 L = normalize(vec2(-0.55, -0.83));
    float ndl = dot(n, L);
    float primary = max(ndl, 0.0);
    float secondary = max(-ndl, 0.0);
    float line = 1.0 - smoothstep(0.0, button ? 1.1 : 1.4, -d);
    vec3 warm = vec3(1.0, 0.86, 0.58);
    vec3 cool = vec3(0.93, 0.98, 1.0);
    vec3 rim = mix(cool, warm, clamp(secondary * 1.4, 0.0, 1.0));
    float rimAmt = line * (0.22 + 0.85 * primary + 0.5 * secondary);
    col = mix(col, rim, clamp(rimAmt, 0.0, 1.0));
    // Wider Fresnel falloff under the rim.
    col += rim * pow(edge, 3.0) * (0.10 + 0.22 * primary + 0.12 * secondary);

    // ---- delicate pearl iridescence, only along the beveled glass edge ----
    // Time shifts the hue very slowly, not the geometry. The central world
    // view and item slots stay clear; no full-panel rainbow or flashing overlay.
    float edgeZone = 1.0 - smoothstep(0.7, button ? 2.4 : 6.0, -d);
    float topEdge = 1.0 - smoothstep(-0.78, -0.18, n.y);
    float sideEdge = abs(n.x);
    float cornerGlow = smoothstep(0.68, 0.97, abs(local.x) / max(HalfSize.x, 1.0));
    float spectralPhase = 1.7 * local.x / max(HalfSize.x, 1.0)
                        + 0.35 * local.y / max(HalfSize.y, 1.0)
                        + Time * 0.11;
    vec3 spectrum = 0.53 + 0.47 * cos(spectralPhase + vec3(0.0, -2.0944, 2.0944));
    vec3 pearlSpectrum = mix(vec3(0.97, 0.99, 1.0), spectrum, 0.63);
    float spectrumMask = edgeZone * (0.54 * topEdge + 0.12 * sideEdge
                                   + 0.22 * cornerGlow);
    float spectrumWeight = min(0.46, spectrumMask) * (button ? 0.32 : 1.0);
    col = mix(col, pearlSpectrum, spectrumWeight);

    // Thin dark contour just inside the rim gives the plate thickness.
    float contour = smoothstep(1.3, 2.2, -d) * (1.0 - smoothstep(2.2, 3.8, -d));
    col *= 1.0 - contour * (button ? 0.05 : 0.08);

    if (!button) {
        // Slow diagonal sheen bands.
        float t = dot(local, normalize(vec2(1.0, 0.75))) / max(length(HalfSize), 1.0);
        t += sin(Time * 0.25) * 0.025;
        float sheen = exp(-pow((t + 0.42) * 5.0, 2.0)) * 0.085
                    + exp(-pow((t - 0.08) * 13.0, 2.0)) * 0.045;
        col += mix(cool, pearlSpectrum, 0.38) * sheen * smoothstep(1.0, 8.0, -d);

        // Soft halo of light gathering at the top edge.
        vec2 hp = (local - vec2(0.0, -HalfSize.y)) / vec2(HalfSize.x * 0.85, HalfSize.y * 0.55);
        col += mix(cool, warm, 0.45) * exp(-length(hp) * 1.7) * 0.13;

        // Sparse twinkles hugging the frame only, never over the item area.
        vec2 cell = floor((local + HalfSize) / 10.0);
        float h = hash(cell);
        if (h > 0.985) {
            vec2 p = fract((local + HalfSize) / 10.0) - 0.5;
            float tw = pow(max(0.0, sin(Time * 1.6 + h * 80.0)), 6.0);
            float star = exp(-length(p) * 13.0) + 0.5 * exp(-abs(p.x) * 30.0 - abs(p.y) * 5.0)
                                                + 0.5 * exp(-abs(p.y) * 30.0 - abs(p.x) * 5.0);
            float zone = 1.0 - smoothstep(3.0, 14.0, -d);
            col += mix(warm, pearlSpectrum, 0.42) * star * tw * zone * 0.38 * step(0.0, -d);
        }
    } else {
        col += tint * Params.y * 0.14;
        float under = (1.0 - smoothstep(0.0, 1.2, abs(local.y - (HalfSize.y - 3.0)))) * Params.y;
        col = mix(col, warm, under * 0.7 * smoothstep(0.0, HalfSize.x * 0.5, HalfSize.x - abs(local.x)));
    }

    // Fine grain removes banding and gives the plate a micro surface.
    col += (hash(gl_FragCoord.xy) - 0.5) * 0.016;

    // ---- floating shadow (pane/button), outside the plate ----
    float sh = 0.0;
    if (!button) {
        float sd = sdBox(local - vec2(0.0, 3.0), HalfSize, Radius);
        sh = (1.0 - smoothstep(-2.0, 9.0, sd)) * 0.30;
    } else {
        float sd = sdBox(local - vec2(0.0, 1.5), HalfSize, Radius);
        sh = (1.0 - smoothstep(-1.0, 4.0, sd)) * 0.22;
    }
    sh *= 1.0 - cover;
    vec3 shadowCol = vec3(0.03, 0.07, 0.12);
    float a = cover + sh * (1.0 - cover);
    if (a < 0.003) discard;
    vec3 rgb = (clamp(col, 0.0, 1.0) * cover + shadowCol * sh) / a;
    fragColor = vec4(rgb, a);
}
