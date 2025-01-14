#version 150 core

#define ambientLight 0.6f

in vec2 texCoord0;

out vec4 outColor;

uniform sampler2D diffuse;
uniform sampler2D emission;

uniform float lightLevel;

uniform bool useLight;

#process

void main() {
    outColor = process(texture(diffuse, texCoord0));

    if (outColor.a < 0.01) discard;

    if(useLight) outColor.xyz *= max(texture(emission, texCoord0).r, lightLevel);
}
