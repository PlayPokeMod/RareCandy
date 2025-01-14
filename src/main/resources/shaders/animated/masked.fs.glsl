#version 150 core

#define ambientLight 0.6f

in vec2 texCoord0;

out vec4 outColor;

uniform sampler2D diffuse;
uniform sampler2D mask;
uniform sampler2D emission;
uniform vec3 color;

uniform float lightLevel;
uniform bool useLight;

#process

void main() {
    outColor = texture(diffuse, texCoord0);
    if (outColor.a < 0.01) discard;

    float mask = texture(mask, texCoord0).x;

    outColor.xyz = mix(outColor.xyz, outColor.xyz * color, mask);

    outColor = process(outColor);

    if(useLight) outColor.xyz *= max(texture(emission, texCoord0).r, lightLevel);
}
