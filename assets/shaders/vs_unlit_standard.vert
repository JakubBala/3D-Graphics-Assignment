#version 330 core

layout (location = 0) in vec3 position;
layout (location = 1) in vec3 normal;      // Not used but kept for compatibility
layout (location = 2) in vec2 texCoord;

out vec3 aPos;               
out vec3 aNormal;            
out vec2 aTexCoord;

uniform mat4 model;
uniform mat4 mvpMatrix;

uniform vec2 uvScale = vec2(1.0, 1.0);
uniform vec2 uvOffset = vec2(0.0, 0.0);

void main() {
    gl_Position = mvpMatrix * vec4(position, 1.0);
    aPos = vec3(model*vec4(position, 1.0f));

    mat3 normalMatrix = mat3(transpose(inverse(model)));
    aNormal = normalize(normalMatrix * normal);

    aTexCoord = texCoord * uvScale + uvOffset;
}