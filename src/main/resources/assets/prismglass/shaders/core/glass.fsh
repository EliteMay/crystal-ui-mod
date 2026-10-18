#version 150

// Prism Glass v0.9: screen-space crystal glass with single-light facet optics.
// Independent implementation of public techniques (rounded-box SDF, Cauchy-style spectral
// dispersion sampled at 7 wavelengths, Blinn half-vector specular, Schlick-style falloff).
//
// Sampler0 = blurred copy of the scene, Sampler1 = sharp copy of the scene.
// Mode 0 = pane, 1 = etched slot overlay (no backdrop sampling), 2 = button.
// Params: x = tint veil, y = hover, z = blur mix, w = edge refraction (GUI px).
//
// One virtual light drives every highlight, dispersion fringe, bloom and glint. Colours are
// never painted by screen position or time: they come from bevel slope vs. the light's half
// vector, so the rainbow only exists where a real facet would catch the light.
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

// Spectral colours, red -> violet. Dividing by the per-channel sum makes the 7 weights add
// up to exactly white, so a uniform backdrop stays neutral and only contrast edges split.
const vec3 SPEC[7] = vec3[7](
    vec3(1.00, 0.05, 0.00), vec3(1.00, 0.45, 0.00), vec3(0.90, 0.90, 0.00),
    vec3(0.05, 0.90, 0.10), vec3(0.00, 0.80, 0.90), vec3(0.05, 0.20, 1.00),
    vec3(0.50, 0.00, 0.90));
const vec3 SPEC_NORM = vec3(1.0 / 3.5, 1.0 / 3.3, 1.0 / 2.9);

const vec3 SILVER = vec3(0.95, 0.98, 1.00);
const vec3 GOLD = vec3(1.00, 0.84, 0.55);

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

// Four-point glint: a hot core with two thin rays along the frame.
float glint(vec2 p) {
    float core = exp(-length(p) * 0.9);
    float rays = exp(-abs(p.x) / 7.0) * exp(-abs(p.y) * 1.6)
               + exp(-abs(p.y) / 7.0) * exp(-abs(p.x) * 1.6);
    float fade = 1.0 - smoothstep(8.0, 14.0, max(abs(p.x), abs(p.y)));
    return (core + 0.6 * rays) * fade;
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

    // ---- the one light: upper left, slowly breathing by a few degrees ----
    float ang = sin(Time * 0.13) * 0.06;
    float ca = cos(ang), sa = sin(ang);
    vec2 Lraw = vec2(-0.35, -0.75);
    vec3 L3 = normalize(vec3(ca * Lraw.x - sa * Lraw.y, sa * Lraw.x + ca * Lraw.y, 0.55));
    vec3 Hh = normalize(L3 + vec3(0.0, 0.0, 1.0));
    vec2 ldir = normalize(Hh.xy);     // screen direction toward the light
    float s0 = length(Hh.xy);         // bevel slope that mirrors the light into the eye

    // ---- bevel: sin(tilt) falls from a steep fillet to the flat plate ----
    float bevel = min(button ? 3.5 : 9.0, min(HalfSize.x, HalfSize.y));
    float cover = 1.0 - smoothstep(-aa * 0.5, aa * 0.5, d);
    float depth = clamp(-d / bevel, 0.0, 1.0);
    float edge = 1.0 - depth;
    float bulge = edge * edge;
    float slope = 0.85 * pow(edge, 1.6);

    float lit = max(dot(n, ldir), 0.0);
    float back = max(dot(n, -ldir), 0.0);

    // ---- spectral refraction: 7 wavelengths bend by slightly different amounts ----
    vec2 uv0 = gl_FragCoord.xy / FbSize;
    vec2 duv = (-n * bulge * Params.w) * GuiScale / FbSize;
    vec3 sharp;
    if (depth < 0.999) {
        sharp = vec3(0.0);
        for (int i = 0; i < 7; i++) {
            float t = float(i) / 3.0 - 1.0;
            vec3 tex = textureLod(Sampler1, uv0 + duv * (1.0 + 0.35 * t), 0.0).rgb;
            sharp += tex * (SPEC[i] * SPEC_NORM);
        }
    } else {
        sharp = textureLod(Sampler1, uv0, 0.0).rgb;
    }
    vec3 soft = textureLod(Sampler0, uv0 + duv, 0.0).rgb;
    vec3 col = mix(sharp, soft, mix(Params.z, Params.z * 0.35, bulge));

    // Legibility: keep the scene visible but calm its contrast and brightness spikes.
    col = mix(col, vec3(luma(col)), 0.22);
    vec3 tint = vertexColor.rgb;
    col = mix(col, tint, Params.x);

    // Vertical body gradient: light from above, slightly denser toward the bottom.
    float ty = clamp(local.y / max(HalfSize.y, 1.0) * 0.5 + 0.5, 0.0, 1.0);
    col += tint * (0.07 * (1.0 - ty)) - vec3(0.02, 0.03, 0.04) * ty;

    // ---- silver rim on the lit side, gold on the far side (light that crossed the plate) ----
    float line = 1.0 - smoothstep(0.0, button ? 1.0 : 1.3, -d);
    vec3 rim = mix(SILVER, GOLD, smoothstep(0.2, 0.9, back));
    float rimAmt = line * (0.18 + 0.55 * pow(lit, 1.5) + 0.35 * back);
    col = mix(col, rim, clamp(rimAmt, 0.0, 1.0));
    col += rim * pow(edge, 3.0) * (0.06 + 0.12 * lit + 0.10 * back);

    // ---- prism fringe: spectral specular where the facet mirrors the light ----
    // u measures how far this bevel slope is from the mirror slope. Each wavelength peaks at
    // a slightly different u, so red..violet appear as an ordered stripe, only on lit facets.
    // The bevel is not machined perfectly: a low-frequency ripple of the slope makes the stripe
    // drift in depth, and the lit corner is where the light is brightest, so the fringe fades
    // with distance from it instead of running as a uniform ribbon.
    vec2 litCorner = vec2(-(HalfSize.x - Radius), -(HalfSize.y - Radius)) + Radius * ldir;
    float wob = 0.035 * (sin(local.x * 0.09) + 0.6 * sin(local.x * 0.23 + 2.0))
              + 0.035 * (sin(local.y * 0.11 + 1.0) + 0.6 * sin(local.y * 0.27));
    float env = 0.25 + 0.75 * exp(-length(local - litCorner) / (0.9 * length(HalfSize)) * 2.6);
    float u = (slope + wob - s0) / 0.16;
    vec3 fringe = vec3(0.0);
    vec3 fringeBack = vec3(0.0);
    for (int i = 0; i < 7; i++) {
        float t = float(i) / 3.0 - 1.0;
        float a1 = u - 1.3 * t;
        float a2 = u + 1.3 * t;           // light leaving the far side: reversed order
        vec3 w = SPEC[i] * SPEC_NORM;
        fringe += w * exp(-a1 * a1 * 3.0);
        fringeBack += w * exp(-a2 * a2 * 3.0);
    }
    float alignLit = pow(lit, 6.0) * env;
    float alignBack = pow(back, 6.0) * (0.25 + 0.75 * exp(-length(local + litCorner) / (0.9 * length(HalfSize)) * 2.6));
    float core = exp(-u * u * 1.2);
    float kF = button ? 0.45 : 0.85;
    vec3 prism = fringe * alignLit * kF
               + fringeBack * alignBack * 0.32 * kF * mix(vec3(1.0), GOLD, 0.35)
               + SILVER * core * alignLit * 0.10 * kF;
    // Dispersed light is re-routed energy: dim the base under the fringe so hue survives
    // even over a white snow backdrop instead of clipping to white.
    float fm = clamp(luma(fringe * alignLit + fringeBack * alignBack * 0.32) * 1.1, 0.0, 0.6);
    col = col * (1.0 - fm * 0.55) + prism;

    // Thin dark contour just inside the rim gives the plate thickness.
    float contour = smoothstep(1.3, 2.2, -d) * (1.0 - smoothstep(2.2, 3.8, -d));
    col *= 1.0 - contour * (button ? 0.05 : 0.08);

    // ---- sacred light inside: faint gold scattering gathered toward the lit edge ----
    float scatterEdge = exp(min(d, 0.0) / 9.0);
    float litSoft = smoothstep(0.1, 0.9, dot(n, ldir));
    col += GOLD * scatterEdge * litSoft * (button ? 0.03 : 0.06);
    if (!button) {
        float lg = dot(local / HalfSize, ldir);
        col += GOLD * smoothstep(-0.2, 1.2, lg) * 0.03;
    }

    // ---- glints: where the light is mirrored at the corners (lit corner strong) ----
    float gl = 0.0;
    if (!button) {
        float breath = 0.78 + 0.22 * sin(Time * 0.5);
        vec2 corner = litCorner;
        vec2 corner2 = vec2(HalfSize.x - Radius, HalfSize.y - Radius) - Radius * ldir;
        gl = (glint(local - corner) * breath + 0.35 * glint(local - corner2)) * 0.55;
        col += mix(SILVER, GOLD, 0.25) * gl * step(0.0, -d);
    } else {
        col += tint * Params.y * 0.14;
        float under = (1.0 - smoothstep(0.0, 1.2, abs(local.y - (HalfSize.y - 3.0)))) * Params.y;
        col = mix(col, GOLD, under * 0.7 * smoothstep(0.0, HalfSize.x * 0.5, HalfSize.x - abs(local.x)));
    }

    // Fine grain removes banding and gives the plate a micro surface.
    col += (hash(gl_FragCoord.xy) - 0.5) * 0.016;

    // ---- outside the plate: light bloom on the lit side, shadow on the far side ----
    float litOut = smoothstep(0.05, 0.95, dot(n, ldir));
    float bloomFade = button ? (1.0 - smoothstep(1.5, 6.0, d)) : (1.0 - smoothstep(6.0, 14.0, d));
    float bloom = exp(-max(d, 0.0) / 4.5) * litOut * bloomFade * (button ? 0.14 : 0.30);
    bloom += gl * 0.8 * step(0.0, d);
    bloom = clamp(bloom, 0.0, 0.85);
    float sh;
    if (!button) {
        float sd = sdBox(local - vec2(0.0, 3.0), HalfSize, Radius);
        sh = (1.0 - smoothstep(-2.0, 9.0, sd)) * 0.30;
    } else {
        float sd = sdBox(local - vec2(0.0, 1.5), HalfSize, Radius);
        sh = (1.0 - smoothstep(-1.0, 4.0, sd)) * 0.22;
    }
    sh *= (1.0 - litOut * 0.8) * (1.0 - bloom);
    vec3 shadowCol = vec3(0.03, 0.07, 0.12);
    vec3 bloomCol = mix(SILVER, GOLD, 0.35);
    float ao = 1.0 - (1.0 - sh) * (1.0 - bloom);
    vec3 outRgb = (shadowCol * sh * (1.0 - bloom) + bloomCol * bloom) / max(ao, 1e-4);
    float aoS = ao * (1.0 - cover);

    float a = cover + aoS;
    if (a < 0.003) discard;
    vec3 rgb = (clamp(col, 0.0, 1.0) * cover + outRgb * aoS) / a;
    fragColor = vec4(rgb, a);
}
