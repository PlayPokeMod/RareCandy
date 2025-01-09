#version 150 core
#define ambientLight 0.6f
in vec2 texCoord0;

out vec4 outColor;

uniform sampler2D diffuse;

void main() {
    vec4 baseColor = texture(diffuse, texCoord0);

    vec2 wrappedUV = fract(texCoord0 * 5.0);

    float gradient = sin(wrappedUV.x * 3.14159) * sin(wrappedUV.y * 3.14159);

    gradient = (gradient + 1.0) * 0.5;

    vec3 pastelBlue = vec3(0.8, 0.9, 1.0);
    vec3 pastelPink = vec3(1.0, 0.8, 0.9);

    vec3 pastelColor = mix(pastelBlue, pastelPink, gradient);

    vec3 finalColor = mix(baseColor.rgb, pastelColor, 0.5);

    outColor = vec4(finalColor, baseColor.a);
}
