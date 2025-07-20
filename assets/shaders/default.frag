#version 330 core

// TODO: Make these uniforms
#define TEXTURE_SIZE 16u
#define CLOSE_FRESNEL_COLOR vec4(0.0, 0.5, 0.5, 0.4)
#define FAR_FRESNEL_COLOR vec4(0.0, 0.25, 0.3, 0.95)

in vec2 TexCoord;
in float shadow;
in vec3 position;
in vec3 lighting;
in float ao;
in vec3 vNormal;
flat in uint type;

out vec4 FragColor;

// TODO: Make this do text as well (2)
uniform uint meshType;

uniform vec3 cameraPosition;

uniform vec4 fogColor;
uniform float fogFar;
uniform float fogNear;

uniform sampler2D texture1;

void main() {
    if (meshType == 0u) {
        FragColor = texture(texture1, TexCoord / TEXTURE_SIZE);
        if (FragColor.a == 0) discard;

        float distance = length(position);
        float fogFactor = (fogFar - distance) / (fogFar - fogNear);
        fogFactor = clamp(fogFactor, 0.0, 1.0);

        if (type == 1u) {
            vec3 faceNormal = vec3(0.0, 1.0, 0.0);

            float fresnel = abs(dot(vNormal, faceNormal));
            FragColor *= vec4(vec3(0.5), 1-fresnel);
            FragColor += CLOSE_FRESNEL_COLOR*fresnel+FAR_FRESNEL_COLOR*(1-fresnel);
        }

        FragColor = mix(vec4(lighting, 1.0), FragColor, 1);

        FragColor = vec4(FragColor.rgb * shadow, FragColor.a);

        FragColor = mix(fogColor, FragColor, fogFactor);
        // TODO: Mix with filter color too for water and things.
    } else if (meshType == 1u) {
        FragColor = texture(texture1, TexCoord);
    } else if (meshType == 2u) {

    }
}