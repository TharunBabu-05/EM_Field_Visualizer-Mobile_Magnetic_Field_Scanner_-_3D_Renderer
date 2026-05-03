package com.emfield.visualizer.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.emfield.visualizer.data.MagneticFieldData
import com.emfield.visualizer.data.Vector3
import com.emfield.visualizer.data.CalibrationData
import com.emfield.visualizer.data.BufferStats
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Manages magnetometer sensor with direct hardware access
 * Provides raw uncalibrated data at maximum sampling rate
 */
class MagnetometerManager(
    private val context: Context,
    private val bufferSize: Int = 1000
) : SensorEventListener {
    
    private val sensorManager: SensorManager = 
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    
    private var magnetometerSensor: Sensor? = null
    private var magnetometerUncalibratedSensor: Sensor? = null
    
    private var currentCalibration: CalibrationData = CalibrationData()
    private var isRunning = false
    
    // Native buffer handle
    private var nativeInitialized = false
    
    // Flow for real-time data streaming
    private val _magneticFieldFlow = MutableSharedFlow<MagneticFieldData>(
        replay = 0,
        extraBufferCapacity = 1000,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val magneticFieldFlow: SharedFlow<MagneticFieldData> = _magneticFieldFlow.asSharedFlow()
    
    // High-frequency data buffer for batch processing
    private val highFreqBuffer = mutableListOf<MagneticFieldData>()
    private var lastBufferFlush = 0L
    private val bufferFlushInterval = 50_000_000L // 50ms in nanoseconds
    
    // Performance optimization flags
    private var useHighFrequencyMode = true
    private var adaptiveSampling = true
    private var targetSamplingRate = 200f // Target Hz
    
    // Statistics
    private var sampleCount = 0L
    private var lastSampleTime = 0L
    private var estimatedSamplingRate = 0f
    
    init {
        initializeSensor()
        initializeNativeBuffer()
    }
    
    private fun initializeSensor() {
        // Try to get uncalibrated magnetometer first (preferred)
        magnetometerUncalibratedSensor = sensorManager.getDefaultSensor(
            Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED
        )
        
        if (magnetometerUncalibratedSensor == null) {
            Log.w(TAG, "Uncalibrated magnetometer not available, using calibrated")
            magnetometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        }
        
        val sensor = magnetometerUncalibratedSensor ?: magnetometerSensor
        
        if (sensor != null) {
            Log.i(TAG, "Magnetometer initialized:")
            Log.i(TAG, "  Name: ${sensor.name}")
            Log.i(TAG, "  Vendor: ${sensor.vendor}")
            Log.i(TAG, "  Resolution: ${sensor.resolution} μT")
            Log.i(TAG, "  Max Range: ${sensor.maximumRange} μT")
            Log.i(TAG, "  Power: ${sensor.power} mA")
            Log.i(TAG, "  Min Delay: ${sensor.minDelay} μs")
        } else {
            Log.e(TAG, "No magnetometer sensor available!")
        }
    }
    
    private fun initializeNativeBuffer() {
        try {
            System.loadLibrary("emfield_native")
            nativeInit(bufferSize)
            nativeInitialized = true
            Log.i(TAG, "Native buffer initialized with size $bufferSize")
        } catch (e: UnsatisfiedLinkError) {
            Log.e(TAG, "Failed to load native library: ${e.message}")
            nativeInitialized = false
        }
    }
    
    /**
     * Start collecting magnetometer data
     */
    fun start(): Boolean {
        if (isRunning) {
            Log.w(TAG, "Already running")
            return true
        }
        
        val sensor = magnetometerUncalibratedSensor ?: magnetometerSensor
        if (sensor == null) {
            Log.e(TAG, "No magnetometer available")
            return false
        }
        
        // Register at adaptive sampling rate
        val samplingDelay = if (useHighFrequencyMode) {
            if (adaptiveSampling) {
                // Calculate optimal delay based on sensor capabilities
                val maxRate = 1_000_000f / (sensor.minDelay.toFloat())
                val optimalDelay = if (maxRate >= targetSamplingRate) {
                    SensorManager.SENSOR_DELAY_FASTEST
                } else {
                    // Custom delay for target rate
                    (1_000_000f / targetSamplingRate).toInt()
                }
                optimalDelay
            } else {
                SensorManager.SENSOR_DELAY_FASTEST
            }
        } else {
            SensorManager.SENSOR_DELAY_GAME
        }
        
        val success = sensorManager.registerListener(
            this,
            sensor,
            samplingDelay
        )
        
        if (success) {
            isRunning = true
            sampleCount = 0
            lastSampleTime = System.nanoTime()
            Log.i(TAG, "Magnetometer started")
        } else {
            Log.e(TAG, "Failed to start magnetometer")
        }
        
        return success
    }
    
    /**
     * Stop collecting magnetometer data
     */
    fun stop() {
        if (!isRunning) return
        
        sensorManager.unregisterListener(this)
        isRunning = false
        Log.i(TAG, "Magnetometer stopped. Total samples: $sampleCount, Avg rate: $estimatedSamplingRate Hz")
    }
    
    /**
     * Set calibration data
     */
    fun setCalibration(calibration: CalibrationData) {
        currentCalibration = calibration
        Log.i(TAG, "Calibration updated: ${if (calibration.isValid) "Valid" else "Invalid"}")
    }
    
    override fun onSensorChanged(event: SensorEvent) {
        if (!isRunning) return
        
        val timestamp = event.timestamp
        val currentTime = System.currentTimeMillis()
        
        // Extract magnetic field values
        val (rawX, rawY, rawZ) = when (event.sensor.type) {
            Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED -> {
                // Uncalibrated sensor provides [x, y, z, bias_x, bias_y, bias_z]
                // We want the uncalibrated values for our own calibration
                Triple(event.values[0], event.values[1], event.values[2])
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                // Already calibrated by system
                Triple(event.values[0], event.values[1], event.values[2])
            }
            else -> return
        }
        
        // Send to native buffer if available
        if (nativeInitialized) {
            nativeAddSample(rawX, rawY, rawZ, timestamp)
        }
        
        // Apply calibration
        val rawField = Vector3(rawX, rawY, rawZ)
        val calibratedField = currentCalibration.apply(rawField)
        
        // Create data point (position will be updated by spatial tracking)
        val data = MagneticFieldData(
            timestamp = currentTime,
            position = Vector3.ZERO,  // Updated later by spatial tracking
            field = calibratedField,
            accuracy = event.accuracy
        )
        
        // High-frequency mode: batch process for performance
        if (useHighFrequencyMode) {
            synchronized(highFreqBuffer) {
                highFreqBuffer.add(data)
                
                // Flush buffer at intervals or when full
                val currentNanoTime = System.nanoTime()
                if (highFreqBuffer.size >= 100 || (currentNanoTime - lastBufferFlush) > bufferFlushInterval) {
                    flushHighFreqBuffer()
                    lastBufferFlush = currentNanoTime
                }
            }
        } else {
            // Standard mode: emit immediately
            _magneticFieldFlow.tryEmit(data)
        }
        
        // Update statistics
        sampleCount++
        if (sampleCount % 100 == 0L) {
            val currentNanoTime = System.nanoTime()
            val deltaTime = (currentNanoTime - lastSampleTime) / 1_000_000_000.0f
            estimatedSamplingRate = 100f / deltaTime
            lastSampleTime = currentNanoTime
        }
    }
    
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        val accuracyStr = when (accuracy) {
            SensorManager.SENSOR_STATUS_UNRELIABLE -> "Unreliable"
            SensorManager.SENSOR_STATUS_ACCURACY_LOW -> "Low"
            SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> "Medium"
            SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> "High"
            else -> "Unknown"
        }
        Log.i(TAG, "Magnetometer accuracy changed: $accuracyStr")
    }
    
    /**
     * Get current sampling rate
     */
    fun getSamplingRate(): Float = estimatedSamplingRate
    
    /**
     * Get total samples collected
     */
    fun getSampleCount(): Long = sampleCount
    
    /**
     * Check if sensor is available
     */
    fun isSensorAvailable(): Boolean {
        return magnetometerUncalibratedSensor != null || magnetometerSensor != null
    }
    
    /**
     * Get sensor information
     */
    fun getSensorInfo(): String {
        val sensor = magnetometerUncalibratedSensor ?: magnetometerSensor
        return sensor?.let {
            """
            Name: ${it.name}
            Vendor: ${it.vendor}
            Type: ${if (magnetometerUncalibratedSensor != null) "Uncalibrated" else "Calibrated"}
            Resolution: ${it.resolution} μT
            Max Range: ${it.maximumRange} μT
            Power: ${it.power} mA
            Min Delay: ${it.minDelay} μs (${1_000_000f / it.minDelay} Hz max)
            """.trimIndent()
        } ?: "No sensor available"
    }
    
    /**
     * Enable/disable high-frequency mode
     */
    fun setHighFrequencyMode(enabled: Boolean) {
        useHighFrequencyMode = enabled
        Log.i(TAG, "High-frequency mode: ${if (enabled) "ON" else "OFF"}")
    }
    
    /**
     * Set target sampling rate for adaptive sampling
     */
    fun setTargetSamplingRate(rate: Float) {
        targetSamplingRate = rate.coerceIn(50f, 500f)
        Log.i(TAG, "Target sampling rate: ${targetSamplingRate}Hz")
    }
    
    /**
     * Enable/disable adaptive sampling
     */
    fun setAdaptiveSampling(enabled: Boolean) {
        adaptiveSampling = enabled
        Log.i(TAG, "Adaptive sampling: ${if (enabled) "ON" else "OFF"}")
    }
    
    /**
     * Get buffer statistics
     */
    fun getBufferStats(): BufferStats {
        synchronized(highFreqBuffer) {
            return BufferStats(
                bufferSize = highFreqBuffer.size,
                flushInterval = bufferFlushInterval,
                lastFlushTime = lastBufferFlush
            )
        }
    }
    
    /**
     * Force flush high-frequency buffer
     */
    fun flushBuffer() {
        if (useHighFrequencyMode) {
            synchronized(highFreqBuffer) {
                flushHighFreqBuffer()
            }
        }
    }
    
    private fun flushHighFreqBuffer() {
        if (highFreqBuffer.isNotEmpty()) {
            // Process batch for better performance
            val batchSize = highFreqBuffer.size
            for (i in 0 until batchSize) {
                _magneticFieldFlow.tryEmit(highFreqBuffer[i])
            }
            highFreqBuffer.clear()
            
            if (batchSize > 0) {
                Log.v(TAG, "Flushed $batchSize samples from high-frequency buffer")
            }
        }
    }
    
    /**
     * Cleanup resources
     */
    fun cleanup() {
        stop()
        synchronized(highFreqBuffer) {
            highFreqBuffer.clear()
        }
        if (nativeInitialized) {
            nativeCleanup()
            nativeInitialized = false
        }
    }
    
    // Native methods
    private external fun nativeInit(bufferSize: Int)
    private external fun nativeAddSample(x: Float, y: Float, z: Float, timestamp: Long)
    private external fun nativeGetLatest(): FloatArray
    private external fun nativeCleanup()
    
    companion object {
        private const val TAG = "MagnetometerManager"
        
        init {
            try {
                System.loadLibrary("emfield_native")
            } catch (e: UnsatisfiedLinkError) {
                Log.e(TAG, "Failed to load native library: ${e.message}")
            }
        }
    }
}
