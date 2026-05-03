package com.emfield.visualizer.rendering

import android.content.Context
import android.opengl.GLES30
import com.emfield.visualizer.data.Vector3
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * Renders a 3D reference grid
 */
class GridRenderer(private val context: Context) {
    
    private var program = 0
    private var vertexBuffer: FloatBuffer? = null
    private var colorBuffer: FloatBuffer? = null
    private var vertexCount = 0
    
    private var positionHandle = 0
    private var colorHandle = 0
    private var mvpMatrixHandle = 0
    
    init {
        setupShaders()
        generateGrid()
    }
    
    private fun setupShaders() {
        val vertexShaderCode = """
            #version 300 es
            uniform mat4 uMVPMatrix;
            layout(location = 0) in vec3 aPosition;
            layout(location = 1) in vec3 aColor;
            out vec3 vColor;
            
            void main() {
                gl_Position = uMVPMatrix * vec4(aPosition, 1.0);
                vColor = aColor;
            }
        """.trimIndent()
        
        val fragmentShaderCode = """
            #version 300 es
            precision mediump float;
            in vec3 vColor;
            out vec4 fragColor;
            
            void main() {
                fragColor = vec4(vColor, 0.3);
            }
        """.trimIndent()
        
        val vertexShader = loadShader(GLES30.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES30.GL_FRAGMENT_SHADER, fragmentShaderCode)
        
        program = GLES30.glCreateProgram().also {
            GLES30.glAttachShader(it, vertexShader)
            GLES30.glAttachShader(it, fragmentShader)
            GLES30.glLinkProgram(it)
            
            val linkStatus = IntArray(1)
            GLES30.glGetProgramiv(it, GLES30.GL_LINK_STATUS, linkStatus, 0)
            if (linkStatus[0] == 0) {
                val error = GLES30.glGetProgramInfoLog(it)
                GLES30.glDeleteProgram(it)
                throw RuntimeException("Error linking grid program: $error")
            }
        }
        
        positionHandle = GLES30.glGetAttribLocation(program, "aPosition")
        colorHandle = GLES30.glGetAttribLocation(program, "aColor")
        mvpMatrixHandle = GLES30.glGetUniformLocation(program, "uMVPMatrix")
    }
    
    private fun generateGrid() {
        val vertices = mutableListOf<Float>()
        val colors = mutableListOf<Float>()
        
        val gridSize = 2f
        val gridSpacing = 0.5f
        val gridColor = Vector3(0.3f, 0.3f, 0.4f)
        val axisXColor = Vector3(1f, 0.3f, 0.3f)  // Red for X
        val axisYColor = Vector3(0.3f, 1f, 0.3f)  // Green for Y
        val axisZColor = Vector3(0.3f, 0.3f, 1f)  // Blue for Z
        
        // Grid lines on XZ plane (horizontal)
        var x = -gridSize
        while (x <= gridSize) {
            val color = if (x == 0f) axisZColor else gridColor
            
            // Line along Z axis
            vertices.addAll(listOf(x, 0f, -gridSize))
            colors.addAll(listOf(color.x, color.y, color.z))
            
            vertices.addAll(listOf(x, 0f, gridSize))
            colors.addAll(listOf(color.x, color.y, color.z))
            
            x += gridSpacing
        }
        
        var z = -gridSize
        while (z <= gridSize) {
            val color = if (z == 0f) axisXColor else gridColor
            
            // Line along X axis
            vertices.addAll(listOf(-gridSize, 0f, z))
            colors.addAll(listOf(color.x, color.y, color.z))
            
            vertices.addAll(listOf(gridSize, 0f, z))
            colors.addAll(listOf(color.x, color.y, color.z))
            
            z += gridSpacing
        }
        
        // Y axis (vertical)
        vertices.addAll(listOf(0f, -gridSize, 0f))
        colors.addAll(listOf(axisYColor.x, axisYColor.y, axisYColor.z))
        
        vertices.addAll(listOf(0f, gridSize, 0f))
        colors.addAll(listOf(axisYColor.x, axisYColor.y, axisYColor.z))
        
        // Convert to buffers
        vertexCount = vertices.size / 3
        
        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(vertices.toFloatArray())
                position(0)
            }
        
        colorBuffer = ByteBuffer.allocateDirect(colors.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(colors.toFloatArray())
                position(0)
            }
    }
    
    fun draw(mvpMatrix: FloatArray) {
        if (vertexCount == 0) return
        
        // Disable depth testing for grid (always visible)
        GLES30.glDisable(GLES30.GL_DEPTH_TEST)
        
        GLES30.glUseProgram(program)
        
        GLES30.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        
        vertexBuffer?.let { buffer ->
            buffer.position(0)
            GLES30.glEnableVertexAttribArray(positionHandle)
            GLES30.glVertexAttribPointer(
                positionHandle, 3, GLES30.GL_FLOAT, false, 0, buffer
            )
        }
        
        colorBuffer?.let { buffer ->
            buffer.position(0)
            GLES30.glEnableVertexAttribArray(colorHandle)
            GLES30.glVertexAttribPointer(
                colorHandle, 3, GLES30.GL_FLOAT, false, 0, buffer
            )
        }
        
        GLES30.glDrawArrays(GLES30.GL_LINES, 0, vertexCount)
        
        GLES30.glDisableVertexAttribArray(positionHandle)
        GLES30.glDisableVertexAttribArray(colorHandle)
        
        // Re-enable depth testing
        GLES30.glEnable(GLES30.GL_DEPTH_TEST)
    }
    
    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES30.glCreateShader(type).also { shader ->
            GLES30.glShaderSource(shader, shaderCode)
            GLES30.glCompileShader(shader)
            
            val compileStatus = IntArray(1)
            GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compileStatus, 0)
            if (compileStatus[0] == 0) {
                val error = GLES30.glGetShaderInfoLog(shader)
                GLES30.glDeleteShader(shader)
                throw RuntimeException("Error compiling grid shader: $error")
            }
        }
    }
    
    fun cleanup() {
        if (program != 0) {
            GLES30.glDeleteProgram(program)
            program = 0
        }
    }
}
