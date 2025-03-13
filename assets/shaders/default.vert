#version 330 core

#define BITMASK_8 255u
#define BITMASK_4 15u
#define BITMASK_3 7u
#define BITMASK_10 1023u
#define BITMASK_2 3u
#define CHUNK_SIZE vec3(32, 32, 32)
#define SHADOWS float[6](1.2, 0.3, 0.4, 0.6, 0.8, 1.0)// Array of shadow levels

layout(location = 0) in uint data1;
layout(location = 1) in uint data2;

out vec2 TexCoord;
out float shadow;
out vec3 position;
out vec3 lighting;

uniform bool useChunkPosition;
uniform bool useExactPosition;

uniform ivec3 chunkPosition;
uniform vec3 exactPosition;

uniform vec3 cameraPosition;

uniform mat4 view;
uniform mat4 projection;

void main() {
    // Unpack data1
    float x = float((data1 >> 22) & BITMASK_10);
    float y = float((data1 >> 12) & BITMASK_10);
    float z = float((data1 >> 2) & BITMASK_10);
    uint pointType = data1 & BITMASK_2;

    // Unpack data2
    float r = float((data2 >> 28) & BITMASK_4) / float(BITMASK_4);
    float g = float((data2 >> 24) & BITMASK_4) / float(BITMASK_4);
    float b = float((data2 >> 20) & BITMASK_4) / float(BITMASK_4);

    uint normal = (data2 >> 17) & BITMASK_3;

    float u = float((data2 >> 9) & BITMASK_8);
    float v = float((data2 >> 1) & BITMASK_8);
    TexCoord = vec2(u, v);

    vec3 xyz = vec3(x, y, z);

    if (useChunkPosition) {
        xyz = round(xyz / 2 * CHUNK_SIZE) / CHUNK_SIZE;
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

    // Calculate position and set vertex position
    gl_Position = projection * view * vec4(xyz, 1.0);
}