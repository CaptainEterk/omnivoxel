#version 330 core

#define BITMASK_8 255u
#define BITMASK_4 15u
#define BITMASK_3 7u
#define BITMASK_10 1023u
#define BITMASK_2 3u
#define CHUNK_SIZE vec3(32.0, 32.0, 32.0)

// TODO: Make this a uniform
#define SHADOWS float[6](1.2, 0.3, 0.4, 0.6, 0.8, 1.0)// Array of shadow levels

layout(location = 0) in uint data1;
layout(location = 1) in uint data2;

out vec2 TexCoord;
out float shadow;
out vec3 position;
out vec3 lighting;
out float ao;
out float fresnel;

uniform bool useChunkPosition;
uniform bool useExactPosition;

uniform ivec3 chunkPosition;
uniform vec3 exactPosition;

uniform vec3 cameraPosition;

uniform mat4 view;
uniform mat4 projection;
uniform mat4 model;

float simpleNoise(float x, float z) {
    float n = dot(vec2(x, z), vec2(127.1, 311.7));// Create a seed value
    return cos(sin(n) * 13.71632);// Fractal noise using sine function
}

void main() {
    uint x = (data1 >> 22u) & BITMASK_10;
    uint y = (data1 >> 12u) & BITMASK_10;
    uint z = (data1 >> 2u)  & BITMASK_10;

    ao = float(data1 & BITMASK_2);

    vec3 xyz = vec3(x, y, z);

    // Unpack data2
    float r = float((data2 >> 28) & BITMASK_4) / float(BITMASK_4);
    float g = float((data2 >> 24) & BITMASK_4) / float(BITMASK_4);
    float b = float((data2 >> 20) & BITMASK_4) / float(BITMASK_4);

    uint normal = (data2 >> 17) & BITMASK_3;

    float u = float((data2 >> 9) & BITMASK_8);
    float v = float((data2 >> 1) & BITMASK_8);
    TexCoord = vec2(u, v);

    if (useChunkPosition) {
        //xyz = round(xyz * CHUNK_SIZE) / CHUNK_SIZE/32.0;
        xyz *= 0.0625;
        xyz += chunkPosition*CHUNK_SIZE;

        // Shadow calculation
        shadow = (normal < 6u) ? SHADOWS[normal] : 1.0;
    }
    if (useExactPosition) {
        xyz += exactPosition;
        xyz /= 32;

        shadow = 1.0;
    }

    position = xyz;
    lighting = vec3(r, g, b);

    vec3 toCameraVector = cameraPosition - xyz;
    vec3 viewVector = normalize(toCameraVector);
    vec3 faceNormal = vec3(0.0, 1.0, 0.0);
    fresnel = abs(dot(viewVector, faceNormal));

    // Calculate position and set vertex position
    gl_Position = projection * view * model * vec4(xyz, 1.0);
}