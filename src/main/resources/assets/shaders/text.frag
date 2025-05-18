#version 330 core
in vec2 TexCoord;
out vec4 FragColor;

uniform sampler2D textTexture;
uniform vec3 textColor;

void main() {
    float alpha = texture(textTexture, TexCoord).r;
    FragColor = vec4(textColor, alpha);
}