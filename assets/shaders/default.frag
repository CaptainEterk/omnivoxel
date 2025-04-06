#version 330 core

#define TEXTURE_SIZE 16u
#define CLOSE_FRESNEL_COLOR vec4(0.0, 0.5, 0.5, 0.4)
#define FAR_FRESNEL_COLOR vec4(0.0, 0.25, 0.3, 0.95)

in vec2 TexCoord;
in float shadow;
in vec3 position;
in vec3 lighting;
in float ao;
in float fresnel;

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

    if (FragColor.a < 1) {
        // Water
        FragColor *= vec4(vec3(0.5), 1-fresnel);
        FragColor += CLOSE_FRESNEL_COLOR*fresnel+FAR_FRESNEL_COLOR*(1-fresnel);
    }

    FragColor = mix(vec4(lighting, 1.0), FragColor, 1.0);

    // Apply shadow effect
    FragColor = vec4(FragColor.rgb * shadow, FragColor.a);

    // Apply fog effect
    FragColor = mix(fogColor, FragColor, fogFactor);
    // TODO: Mix with filter color too.
}