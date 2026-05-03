#version 300 es
precision mediump float;

// Fragment shader for rendering magnetic field vectors

in vec3 vColor;
in vec3 vNormal;
in vec3 vPosition;

out vec4 fragColor;

uniform vec3 uLightPos;       // Light position in world space
uniform vec3 uCameraPos;      // Camera position for specular

void main() {
    // Normalize interpolated normal
    vec3 normal = normalize(vNormal);
    
    // Ambient lighting
    float ambientStrength = 0.3;
    vec3 ambient = ambientStrength * vColor;
    
    // Diffuse lighting
    vec3 lightDir = normalize(uLightPos - vPosition);
    float diff = max(dot(normal, lightDir), 0.0);
    vec3 diffuse = diff * vColor;
    
    // Specular lighting
    float specularStrength = 0.5;
    vec3 viewDir = normalize(uCameraPos - vPosition);
    vec3 reflectDir = reflect(-lightDir, normal);
    float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32.0);
    vec3 specular = specularStrength * spec * vec3(1.0, 1.0, 1.0);
    
    // Combine lighting
    vec3 result = ambient + diffuse + specular;
    fragColor = vec4(result, 1.0);
}
