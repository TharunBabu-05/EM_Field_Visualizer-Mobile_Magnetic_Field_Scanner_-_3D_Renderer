package com.emfield.visualizer.ui

import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.emfield.visualizer.R
import com.emfield.visualizer.rendering.GLRenderer
import com.emfield.visualizer.sensors.MagnetometerManager
import com.emfield.visualizer.data.MagneticFieldData
import kotlinx.coroutines.launch

/**
 * Enhanced VisualizationFragment with OpenGL rendering
 */
class VisualizationFragment : Fragment() {
    
    private lateinit var glSurfaceView: GLSurfaceView
    private lateinit var glRenderer: GLRenderer
    private lateinit var magnetometerManager: MagnetometerManager
    private lateinit var modeText: TextView
    private lateinit var exportButton: Button
    
    private var currentMode = GLRenderer.VisualizationMode.VECTORS
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_visualization, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize OpenGL surface
        setupOpenGL(view)
        
        // Initialize sensor manager
        magnetometerManager = MagnetometerManager(requireContext())
        
        // Initialize UI controls
        setupUI(view)
        
        // Start sensor data collection
        setupSensorListener()
    }
    
    private fun setupOpenGL(view: View) {
        glSurfaceView = view.findViewById(R.id.gl_surface_view)
        glRenderer = GLRenderer()
        
        glSurfaceView.apply {
            setEGLContextClientVersion(3)
            setRenderer(glRenderer)
            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        }
    }
    
    private fun setupUI(view: View) {
        // Find UI elements in the control panel
        modeText = view.findViewById(R.id.viz_mode_title)
        exportButton = view.findViewById(R.id.export_button)
        
        // Verify UI elements are found
        if (modeText == null) {
            android.util.Log.e("VisualizationFragment", "modeText not found!")
            return
        }
        
        // Set up mode switching with better error handling
        modeText.setOnClickListener {
            try {
                android.util.Log.d("VisualizationFragment", "Mode text clicked!")
                cycleVisualizationMode()
            } catch (e: Exception) {
                android.util.Log.e("VisualizationFragment", "Error cycling mode", e)
            }
        }
        
        exportButton.setOnClickListener {
            try {
                android.util.Log.d("VisualizationFragment", "Export button clicked!")
                exportData()
            } catch (e: Exception) {
                android.util.Log.e("VisualizationFragment", "Error exporting data", e)
            }
        }
        
        updateModeDisplay()
        android.util.Log.d("VisualizationFragment", "UI setup completed")
    }
    
    private fun setupSensorListener() {
        lifecycleScope.launch {
            magnetometerManager.magneticFieldFlow.collect { data ->
                updateVisualization(data)
            }
        }
    }
    
    private fun updateVisualization(data: MagneticFieldData) {
        // Send data to OpenGL renderer
        glRenderer.updateMagneticFieldData(listOf(data))
    }
    
    private fun cycleVisualizationMode() {
        try {
            android.util.Log.d("VisualizationFragment", "Cycling from mode: $currentMode")
            
            currentMode = when (currentMode) {
                GLRenderer.VisualizationMode.VECTORS -> {
                    android.util.Log.d("VisualizationFragment", "Switching to HEATMAP mode")
                    GLRenderer.VisualizationMode.HEATMAP
                }
                GLRenderer.VisualizationMode.HEATMAP -> {
                    android.util.Log.d("VisualizationFragment", "Switching to PARTICLES mode")
                    GLRenderer.VisualizationMode.PARTICLES
                }
                GLRenderer.VisualizationMode.PARTICLES -> {
                    android.util.Log.d("VisualizationFragment", "Switching to VECTORS mode")
                    GLRenderer.VisualizationMode.VECTORS
                }
            }
            
            glRenderer.setVisualizationMode(currentMode)
            updateModeDisplay()
            android.util.Log.d("VisualizationFragment", "Mode cycling completed successfully")
        } catch (e: Exception) {
            android.util.Log.e("VisualizationFragment", "Error in cycleVisualizationMode", e)
        }
    }
    
    private fun updateModeDisplay() {
        val modeName = when (currentMode) {
            GLRenderer.VisualizationMode.VECTORS -> "Vector Field"
            GLRenderer.VisualizationMode.HEATMAP -> "Heatmap"
            GLRenderer.VisualizationMode.PARTICLES -> "Particle Flow"
        }
        modeText.text = "Mode: $modeName"
    }
    
    private fun exportData() {
        // Placeholder for data export functionality
        // Will be implemented with the ExportManager
        lifecycleScope.launch {
            try {
                // Collect some sample data and export
                val sampleData = listOf<MagneticFieldData>()
                // Export logic here
            } catch (e: Exception) {
                // Handle export error
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        glSurfaceView.onResume()
    }
    
    override fun onPause() {
        super.onPause()
        glSurfaceView.onPause()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        glSurfaceView.queueEvent {
            glRenderer.cleanup()
        }
        magnetometerManager.cleanup()
    }
}
