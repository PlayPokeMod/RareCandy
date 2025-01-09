#version 150 core
#define ambientLight 0.6f
in vec2 texCoord0;

out vec4 outColor;

uniform sampler2D diffuse;

void main() {
    vec4 color = texture(diffuse, texCoord0);

    float grayscale = 0.2126 * color.r + 0.7152 * color.g + 0.0722 * color.b;

    float luminanceDx = dFdx(grayscale);
    float luminanceDy = dFdy(grayscale);
    float edgeFactor = length(vec2(luminanceDx, luminanceDy));

    float outline = 1.0 - smoothstep(0.02, 0.05, edgeFactor);
    vec3 edgeColor = vec3(0.0);

    vec3 finalColor = mix(vec3(grayscale), edgeColor, outline);

    outColor = vec4(finalColor, color.a);
}
