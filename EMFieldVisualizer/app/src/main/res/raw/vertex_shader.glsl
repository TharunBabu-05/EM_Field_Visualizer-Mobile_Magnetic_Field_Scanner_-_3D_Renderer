#version 300 es

// Vertex shader for rendering magnetic field vectors

uniform mat4 uMVPMatrix;     // Model-View-Projection matrix
uniform mat4 uModelMatrix;    // Model matrix for normals

layout(location = 0) in vec3 aPosition;    // Vertex position
layout(location = 1) in vec3 aColor;       // Vertex color (field strength)
layout(location = 2) in vec3 aNormal;      // Normal vector

out vec3 vColor;
out vec3 vNormal;
out vec3 vPosition;

void main() {
    // Transform position
    gl_Position = uMVPMatrix * vec4(aPosition, 1.0);
    
    // Pass color to fragment shader
    vColor = aColor;
    
    // Transform normal
    vNormal = mat3(uModelMatrix) * aNormal;
    
    // World position for lighting
    vPosition = (uModelMatrix * vec4(aPosition, 1.0)).xyz;
}
