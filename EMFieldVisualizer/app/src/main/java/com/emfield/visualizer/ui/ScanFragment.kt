package com.emfield.visualizer.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.emfield.visualizer.R
import com.emfield.visualizer.sensors.MagnetometerManager
import com.emfield.visualizer.data.MagneticFieldData
import kotlinx.coroutines.launch

/**
 * Enhanced ScanFragment with real sensor data collection
 */
class ScanFragment : Fragment() {
    
    private lateinit var magnetometerManager: MagnetometerManager
    private lateinit var fieldStrengthText: TextView
    private lateinit var scanModeButton: Button
    private var isScanning = false
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_scan, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize UI elements
        setupUI(view)
        
        // Initialize sensor manager
        magnetometerManager = MagnetometerManager(requireContext())
        
        // Check permissions
        checkPermissions()
    }
    
    private fun setupUI(view: View) {
        fieldStrengthText = view.findViewById(R.id.field_strength_text)
        scanModeButton = view.findViewById(R.id.scan_mode_switch)
        
        // Add logging to debug button issues
        android.util.Log.d("ScanFragment", "Setting up UI...")
        android.util.Log.d("ScanFragment", "fieldStrengthText: $fieldStrengthText")
        android.util.Log.d("ScanFragment", "scanModeButton: $scanModeButton")
        
        if (scanModeButton == null) {
            android.util.Log.e("ScanFragment", "scanModeButton is NULL!")
            return
        }
        
        scanModeButton.setOnClickListener {
            try {
                android.util.Log.d("ScanFragment", "Button clicked! isScanning: $isScanning")
                toggleScanning()
            } catch (e: Exception) {
                android.util.Log.e("ScanFragment", "Error in button click", e)
            }
        }
        
        // Initialize display
        fieldStrengthText.text = "0.0 μT"
        scanModeButton.text = "Start Scan"
        android.util.Log.d("ScanFragment", "UI setup completed")
    }
    
    private fun checkPermissions() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BODY_SENSORS
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission granted, can start scanning
                setupSensorListener()
            }
            else -> {
                // Request permission
                requestPermissions(
                    arrayOf(Manifest.permission.BODY_SENSORS),
                    PERMISSION_REQUEST_SENSORS
                )
            }
        }
    }
    
    private fun setupSensorListener() {
        lifecycleScope.launch {
            magnetometerManager.magneticFieldFlow.collect { data ->
                updateUI(data)
            }
        }
    }
    
    private fun toggleScanning() {
        android.util.Log.d("ScanFragment", "toggleScanning called. Current state: isScanning=$isScanning")
        
        if (isScanning) {
            android.util.Log.d("ScanFragment", "Stopping scanning...")
            stopScanning()
        } else {
            android.util.Log.d("ScanFragment", "Starting scanning...")
            startScanning()
        }
    }
    
    private fun startScanning() {
        if (!isScanning) {
            android.util.Log.d("ScanFragment", "Starting scanning - updating UI")
            isScanning = true
            scanModeButton.text = "⏹ STOP SCAN"
            fieldStrengthText.text = "Scanning..."
            magnetometerManager.start()
            android.util.Log.d("ScanFragment", "Scanning started - UI updated")
        }
    }
    
    private fun stopScanning() {
        if (isScanning) {
            android.util.Log.d("ScanFragment", "Stopping scanning - updating UI")
            isScanning = false
            scanModeButton.text = "▶ START SCAN"
            fieldStrengthText.text = "0.0 μT"
            magnetometerManager.stop()
            android.util.Log.d("ScanFragment", "Scanning stopped - UI updated")
        }
    }
    
    private fun updateUI(data: MagneticFieldData) {
        val magnitude = data.magnitude()
        fieldStrengthText.text = String.format("%.1f μT", magnitude)
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            PERMISSION_REQUEST_SENSORS -> {
                if (grantResults.isNotEmpty() && 
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupSensorListener()
                } else {
                    fieldStrengthText.text = "Permission denied"
                    scanModeButton.isEnabled = false
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopScanning()
        magnetometerManager.cleanup()
    }
    
    companion object {
        private const val PERMISSION_REQUEST_SENSORS = 1001
    }
}
