#version 330 core

// TODO: Make these uniforms
#define TEXTURE_SIZE 16u
#define CLOSE_FRESNEL_COLOR vec4(0.0, 0.5, 0.5, 0.4)
#define FAR_FRESNEL_COLOR vec4(0.0, 0.25, 0.3, 0.95)

in vec2 TexCoord;
in float shadow;
smooth in vec3 position;
in vec3 lighting;
in float ao;
in vec3 vNormal;
flat in uint blockType;

out vec4 FragColor;

// TODO: Make this do text as well (2)
uniform uint meshType;

uniform vec3 cameraPosition;

uniform vec4 fogColor;
uniform float fogFar;
uniform float fogNear;
uniform float time;

uniform sampler2D texture1;

float hash(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

float cubicWeight(float t) {
    t = abs(t);
    return (t <= 1.0) ? 1.0 - 2.0*t*t + t*t*t : ((t < 2.0) ? 4.0 - 8.0*t + 5.0*t*t - t*t*t : 0.0);
}

float smoothNoiseCubic(vec2 uv) {
    vec2 i = floor(uv);
    vec2 f = fract(uv);
    float result = 0.0;
    float totalWeight = 0.0;

    for(int dx = -1; dx <= 2; dx++) {
        for(int dz = -1; dz <= 2; dz++) {
            vec2 neighbor = i + vec2(float(dx), float(dz));
            float weight = cubicWeight(f.x - float(dx)) * cubicWeight(f.y - float(dz));
            result += hash(neighbor) * weight;
            totalWeight += weight;
        }
    }

    return result / totalWeight;
}

float simpleNoise(vec2 pos) {
    return smoothNoiseCubic(pos);
}

void main() {
    if (meshType == 0u) {
        FragColor = texture(texture1, TexCoord / TEXTURE_SIZE);
        if (FragColor.a == 0) discard;

        float distance = length(position-cameraPosition);
        float fogFactor = (fogFar - distance) / (fogFar - fogNear);
        fogFactor = clamp(fogFactor, 0.0, 1.0);
        if (fogFactor == 0) {
            discard;
        }

        if (blockType == 1u) {
            vec3 faceNormal = vec3(0.0, 1.0, 0.0);

            float fresnel = abs(dot(vNormal, faceNormal));
            FragColor *= vec4(vec3(0.5), 1-fresnel);
//            FragColor *= vec4(simpleNoise(position.xz/2+time/1) / 10.0);
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