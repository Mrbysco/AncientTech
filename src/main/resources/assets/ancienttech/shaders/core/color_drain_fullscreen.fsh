#version 330

uniform sampler2D GrabSampler;

layout(std140) uniform DrainInfo {
    float Grayscale;
    float Brightness;
};

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 color = texture(GrabSampler, texCoord);

    float luma = 0.299 * color.r + 0.587 * color.g + 0.114 * color.b;
    fragColor = vec4(mix(color.rgb, vec3(luma), Grayscale) * Brightness, color.a);
}