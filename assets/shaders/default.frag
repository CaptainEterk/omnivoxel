#version 330 core

#define TEXTURE_SIZE 16u

in vec2 TexCoord;
in float shadow;
in vec3 position;
in vec3 lighting;

out vec4 FragColor;

uniform vec3 cameraPosition;

uniform vec4 fogColor;
uniform float fogFar;
uniform float fogNear;

uniform sampler2D texture1;

void main() {
    // Sample the texture at the pixelated texture coordinate
    FragColor = texture(texture1, TexCoord / TEXTURE_SIZE);
    if (FragColor.a == 0) discard;

    // Fog
    float distance = length(position-cameraPosition);
    float fogFactor = (fogFar - distance) / (fogFar - fogNear);
    fogFactor = clamp(fogFactor, 0.0, 1.0);

    FragColor = mix(vec4(lighting, 1.0), FragColor, 0.1);

    // Apply shadow effect
    FragColor = vec4(FragColor.rgb * shadow, FragColor.a);

    // Apply fog effect
//    FragColor = mix(fogColor, FragColor, fogFactor);
    // TODO: Mix with filter color too.
}