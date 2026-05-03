#version 300 es
precision mediump float;

// Fragment shader for particles

in vec4 vColor;
in float vLife;

out vec4 fragColor;

void main() {
    // Create circular particle
    vec2 coord = gl_PointCoord - vec2(0.5);
    float dist = length(coord);
    
    if (dist > 0.5) {
        discard;  // Outside circle
    }
    
    // Soft edge falloff
    float alpha = vColor.a * (1.0 - smoothstep(0.3, 0.5, dist));
    
    fragColor = vec4(vColor.rgb, alpha);
}
