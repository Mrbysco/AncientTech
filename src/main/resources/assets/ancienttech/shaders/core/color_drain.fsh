#version 330

uniform sampler2D GrabSampler;

layout(std140) uniform DrainInfo {
    float Grayscale;
    float Brightness;
};

out vec4 fragColor;

void main() {
    vec2 screenUv = gl_FragCoord.xy / vec2(textureSize(GrabSampler, 0));
    vec4 color = texture(GrabSampler, screenUv);

    float luma = 0.299 * color.r + 0.587 * color.g + 0.114 * color.b;
    fragColor = vec4(mix(color.rgb, vec3(luma), Grayscale) * Brightness, color.a);
}