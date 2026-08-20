#version 330 core

layout(location = 5) in vec2 aPos;
layout(location = 6) in vec2 aTexCoord;

out vec2 TexCoord;

uniform mat4 projection;

void main() {
    gl_Position =
    projection *
    vec4(aPos, 0.0, 1.0);

    TexCoord = aTexCoord;
}