#version 330 core

#define BITMASK_8 255u
#define BITMASK_4 15u
#define BITMASK_3 7u
#define BITMASK_10 1023u
#define BITMASK_2 3u
#define CHUNK_SIZE vec3(32.0, 32.0, 32.0)

layout(location = 0) in uint data1;
layout(location = 1) in uint data2;

uniform bool useChunkPosition;
uniform bool useExactPosition;

uniform ivec3 chunkPosition;
uniform vec3 exactPosition;

uniform mat4 view;
uniform mat4 projection;
uniform mat4 model;

void main() {
    uint x = (data1 >> 22u) & BITMASK_10;
    uint y = (data1 >> 12u) & BITMASK_10;
    uint z = (data1 >> 2u)  & BITMASK_10;

    vec3 xyz = vec3(x, y, z);

    if (useChunkPosition) {
        //xyz = round(xyz * CHUNK_SIZE) / CHUNK_SIZE/32.0;
        xyz *= 0.0625;
        xyz += chunkPosition*CHUNK_SIZE;
    }
    if (useExactPosition) {
        xyz += exactPosition;
        xyz /= 32;
    }

    gl_Position = projection * view * model * vec4(xyz, 1.0);
}