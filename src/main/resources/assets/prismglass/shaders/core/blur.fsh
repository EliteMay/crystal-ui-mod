#version 150

// Dual Kawase blur (Bjorge, SIGGRAPH 2015 public technique), independent implementation.
// Mode 0 = copy, 1 = downsample, 2 = upsample.
uniform sampler2D Sampler0;
uniform float Mode;
uniform vec2 HalfPixel;
uniform float Offset;

in vec2 uv;
out vec4 fragColor;

void main() {
    vec2 o = HalfPixel * Offset;
    if (Mode < 0.5) {
        fragColor = vec4(texture(Sampler0, uv).rgb, 1.0);
    } else if (Mode < 1.5) {
        vec3 sum = texture(Sampler0, uv).rgb * 4.0;
        sum += texture(Sampler0, uv - o).rgb;
        sum += texture(Sampler0, uv + o).rgb;
        sum += texture(Sampler0, uv + vec2(o.x, -o.y)).rgb;
        sum += texture(Sampler0, uv - vec2(o.x, -o.y)).rgb;
        fragColor = vec4(sum / 8.0, 1.0);
    } else {
        vec3 sum = texture(Sampler0, uv + vec2(-o.x * 2.0, 0.0)).rgb;
        sum += texture(Sampler0, uv + vec2(-o.x, o.y)).rgb * 2.0;
        sum += texture(Sampler0, uv + vec2(0.0, o.y * 2.0)).rgb;
        sum += texture(Sampler0, uv + vec2(o.x, o.y)).rgb * 2.0;
        sum += texture(Sampler0, uv + vec2(o.x * 2.0, 0.0)).rgb;
        sum += texture(Sampler0, uv + vec2(o.x, -o.y)).rgb * 2.0;
        sum += texture(Sampler0, uv + vec2(0.0, -o.y * 2.0)).rgb;
        sum += texture(Sampler0, uv + vec2(-o.x, -o.y)).rgb * 2.0;
        fragColor = vec4(sum / 12.0, 1.0);
    }
}
