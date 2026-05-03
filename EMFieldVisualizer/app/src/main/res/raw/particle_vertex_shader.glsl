#version 300 es

// Vertex shader for particle system

uniform mat4 uMVPMatrix;
uniform float uTime;

layout(location = 0) in vec3 aPosition;
layout(location = 1) in vec3 aVelocity;
layout(location = 2) in float aLifetime;

out vec4 vColor;
out float vLife;

void main() {
    // Update particle position based on velocity and time
    vec3 pos = aPosition + aVelocity * uTime;
    
    gl_Position = uMVPMatrix * vec4(pos, 1.0);
    gl_PointSize = 8.0 * (1.0 - aLifetime);
    
    // Color based on lifetime (fade out)
    vLife = 1.0 - aLifetime;
    float speed = length(aVelocity);
    
    // Color gradient: slow (green) -> medium (yellow) -> fast (red)
    vec3 color = mix(
        vec3(0.3, 1.0, 0.3),  // Green (slow)
        vec3(1.0, 0.3, 0.3),  // Red (fast)
        clamp(speed / 10.0, 0.0, 1.0)
    );
    
    vColor = vec4(color, vLife);
}
