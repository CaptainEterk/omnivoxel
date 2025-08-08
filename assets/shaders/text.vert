#version 330 core

layout(location = 0) in vec2 quadPos;
layout(location = 1) in vec2 quadUV;

layout(location = 2) in vec2 pos;
layout(location = 3) in vec2 size;
layout(location = 4) in vec4 uvBounds;

out vec2 TexCoord;

uniform mat4 projection;

void main() {

    vec2 worldPos = pos + quadPos * size;

    TexCoord = mix(vec2(uvBounds.x, uvBounds.w), vec2(uvBounds.z, uvBounds.y), quadUV);

    gl_Position = projection * vec4(worldPos, 0.0, 1.0);
}