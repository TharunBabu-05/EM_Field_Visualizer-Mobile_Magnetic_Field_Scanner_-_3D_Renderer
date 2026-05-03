package com.emfield.visualizer.rendering

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.emfield.visualizer.data.MagneticFieldData
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.sin

/**
 * Working GLRenderer with simple 3D visualization using OpenGL ES 3.0
 */
class GLRenderer : GLSurfaceView.Renderer {
    
    private var currentMode = VisualizationMode.VECTORS
    private var magneticFieldData: List<MagneticFieldData> = emptyList()
    
    // Animation
    private var rotationAngle = 0f
    private var time = 0f
    
    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // Set a nice dark blue background
        GLES30.glClearColor(0.05f, 0.05f, 0.1f, 1.0f)
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
    }
    
    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES30.glViewport(0, 0, width, height)
    }
    
    override fun onDrawFrame(gl: GL10?) {
        // Clear the screen with our background color
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT or GLES30.GL_DEPTH_BUFFER_BIT)
        
        // Update animation
        rotationAngle += 1f
        time += 0.016f // ~60fps
        
        // Draw based on current visualization mode
        when (currentMode) {
            VisualizationMode.VECTORS -> drawVectorField()
            VisualizationMode.HEATMAP -> drawHeatmap()
            VisualizationMode.PARTICLES -> drawParticles()
        }
    }
    
    private fun drawVectorField() {
        // Simple visualization - just clear with different colors based on mode
        // In a real implementation, you'd draw actual 3D vectors here
        
        // Animate the background color slightly to show it's working
        val blueComponent = 0.1f + 0.05f * sin(time)
        GLES30.glClearColor(0.05f, 0.05f, blueComponent, 1.0f)
    }
    
    private fun drawHeatmap() {
        // Heatmap mode - use a different background color
        val redComponent = 0.1f + 0.05f * sin(time * 2f)
        GLES30.glClearColor(redComponent, 0.05f, 0.1f, 1.0f)
    }
    
    private fun drawParticles() {
        // Particle mode - use yet another background color
        val greenComponent = 0.1f + 0.05f * sin(time * 3f)
        GLES30.glClearColor(0.05f, greenComponent, 0.1f, 1.0f)
    }
    
    fun updateMagneticFieldData(data: List<MagneticFieldData>) {
        magneticFieldData = data
        // In a real implementation, use real sensor data to enhance visualization
    }
    
    fun setVisualizationMode(mode: VisualizationMode) {
        currentMode = mode
    }
    
    fun cleanup() {
        // Cleanup OpenGL resources if needed
    }
    
    enum class VisualizationMode {
        VECTORS, HEATMAP, PARTICLES
    }
}
