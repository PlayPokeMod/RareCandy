#version 150 core
#define ambientLight 0.6f
in vec2 texCoord0;

out vec4 outColor;

uniform sampler2D diffuse;

void main() {
    vec4 baseColor = texture(diffuse, texCoord0);

    float luminance = dot(baseColor.rgb, vec3(0.299, 0.587, 0.114));
    vec3 grayscaleColor = vec3(luminance);

    vec3 shadowBase = vec3(0.04, 0.04, 0.1);
    vec3 shadowHighlight = vec3(0.07, 0.07, 0.08);

    vec3 shadowColor = mix(shadowBase, shadowHighlight, smoothstep(0.3, 0.7, luminance));

    vec3 finalColor = mix(grayscaleColor, shadowColor, 0.9);

    finalColor = finalColor * 1.1 - 0.05;

    finalColor = clamp(finalColor, 0.0, 1.0);

    outColor = vec4(finalColor, baseColor.a);
}
