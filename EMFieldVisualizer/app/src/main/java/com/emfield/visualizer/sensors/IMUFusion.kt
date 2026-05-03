package com.emfield.visualizer.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import com.emfield.visualizer.data.Quaternion
import com.emfield.visualizer.data.Vector3
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.sqrt

/**
 * IMU sensor fusion for 6DOF position tracking
 * Combines accelerometer and gyroscope data
 */
class IMUFusion(
    private val context: Context
) : SensorEventListener {
    
    private val sensorManager: SensorManager = 
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    
    private var accelerometerSensor: Sensor? = null
    private var gyroscopeSensor: Sensor? = null
    
    private var isRunning = false
    
    // State tracking
    private var currentPosition = Vector3.ZERO
    private var currentVelocity = Vector3.ZERO
    private var currentOrientation = Quaternion.IDENTITY
    
    private var lastAcceleration = Vector3.ZERO
    private var lastGyroscope = Vector3.ZERO
    private var lastWorldAcceleration: Vector3? = null
    
    private var lastUpdateTime = 0L
    
    // Gravity vector for removing gravity from acceleration
    private var gravity = Vector3(0f, 0f, -9.81f)
    
    // Flow for position updates
    private val _positionFlow = MutableStateFlow(Vector3.ZERO)
    val positionFlow: StateFlow<Vector3> = _positionFlow.asStateFlow()
    
    private val _orientationFlow = MutableStateFlow(Quaternion.IDENTITY)
    val orientationFlow: StateFlow<Quaternion> = _orientationFlow.asStateFlow()
    
    // Native Kalman filter
    private var useNativeFilter = false
    
    init {
        initializeSensors()
        initializeNativeFilter()
    }
    
    private fun initializeSensors() {
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        
        if (accelerometerSensor == null || gyroscopeSensor == null) {
            Log.e(TAG, "Required sensors not available!")
        } else {
            Log.i(TAG, "IMU sensors initialized:")
            Log.i(TAG, "  Accelerometer: ${accelerometerSensor?.name}")
            Log.i(TAG, "  Gyroscope: ${gyroscopeSensor?.name}")
        }
    }
    
    private fun initializeNativeFilter() {
        try {
            System.loadLibrary("emfield_native")
            nativeResetFilter()
            useNativeFilter = true
            Log.i(TAG, "Native Kalman filter initialized")
        } catch (e: UnsatisfiedLinkError) {
            Log.w(TAG, "Native filter not available, using Kotlin implementation")
            useNativeFilter = false
        }
    }
    
    /**
     * Start IMU tracking
     */
    fun start(): Boolean {
        if (isRunning) return true
        
        if (accelerometerSensor == null || gyroscopeSensor == null) {
            Log.e(TAG, "Cannot start: sensors not available")
            return false
        }
        
        val accelSuccess = sensorManager.registerListener(
            this,
            accelerometerSensor,
            SensorManager.SENSOR_DELAY_FASTEST
        )
        
        val gyroSuccess = sensorManager.registerListener(
            this,
            gyroscopeSensor,
            SensorManager.SENSOR_DELAY_FASTEST
        )
        
        if (accelSuccess && gyroSuccess) {
            isRunning = true
            lastUpdateTime = System.nanoTime()
            reset()
            Log.i(TAG, "IMU tracking started")
            return true
        } else {
            Log.e(TAG, "Failed to start IMU tracking")
            return false
        }
    }
    
    /**
     * Stop IMU tracking
     */
    fun stop() {
        if (!isRunning) return
        
        sensorManager.unregisterListener(this)
        isRunning = false
        Log.i(TAG, "IMU tracking stopped")
    }
    
    /**
     * Reset position and orientation
     */
    fun reset() {
        currentPosition = Vector3.ZERO
        currentVelocity = Vector3.ZERO
        currentOrientation = Quaternion.IDENTITY
        lastAcceleration = Vector3.ZERO
        lastGyroscope = Vector3.ZERO
        gravity = Vector3(0f, 0f, -9.81f)
        
        if (useNativeFilter) {
            nativeResetFilter()
        }
        
        _positionFlow.value = currentPosition
        _orientationFlow.value = currentOrientation
        
        Log.i(TAG, "IMU state reset")
    }
    
    override fun onSensorChanged(event: SensorEvent) {
        if (!isRunning) return
        
        val currentTime = System.nanoTime()
        val dt = if (lastUpdateTime > 0) {
            (currentTime - lastUpdateTime) / 1_000_000_000f  // Convert to seconds
        } else {
            0.01f  // Initial delta time
        }
        lastUpdateTime = currentTime
        
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                handleAccelerometer(event.values, dt)
            }
            Sensor.TYPE_GYROSCOPE -> {
                handleGyroscope(event.values, dt)
            }
        }
    }
    
    private fun handleAccelerometer(values: FloatArray, dt: Float) {
        val rawAccel = Vector3(values[0], values[1], values[2])
        
        // Low-pass filter to estimate gravity
        val alpha = 0.8f
        gravity = Vector3(
            alpha * gravity.x + (1 - alpha) * rawAccel.x,
            alpha * gravity.y + (1 - alpha) * rawAccel.y,
            alpha * gravity.z + (1 - alpha) * rawAccel.z
        )
        
        // Remove gravity to get linear acceleration
        val linearAccel = rawAccel - gravity
        
        // Transform to world coordinates
        val worldAccel = currentOrientation.rotateVector(linearAccel)
        
        lastAcceleration = linearAccel
        lastWorldAcceleration = worldAccel
    }
    
    private fun handleGyroscope(values: FloatArray, dt: Float) {
        val angularVel = Vector3(values[0], values[1], values[2])
        lastGyroscope = angularVel
        
        // Apply native Kalman filter if available
        if (useNativeFilter && lastWorldAcceleration != null) {
            val result = nativeKalmanFilter(
                lastWorldAcceleration!!.x, lastWorldAcceleration!!.y, lastWorldAcceleration!!.z,
                angularVel.x, angularVel.y, angularVel.z,
                dt
            )
            
            // Update position from native filter
            currentPosition = Vector3(result[0], result[1], result[2])
            
            // Update orientation from native filter
            currentOrientation = Quaternion(
                result[3], result[4], result[5], result[6]
            )
        } else {
            // Fallback to Kotlin implementation
            handleGyroscopeKotlin(angularVel, dt)
        }
        
        // Emit updated position and orientation
        _positionFlow.value = currentPosition
        _orientationFlow.value = currentOrientation
    }
    
    private fun handleGyroscopeKotlin(angularVel: Vector3, dt: Float) {
        // Integrate angular velocity to get orientation change
        val angle = angularVel.magnitude() * dt
        
        if (angle > 0.001f) {  // Avoid division by zero
            val axis = angularVel.normalized()
            val deltaRotation = Quaternion.fromAxisAngle(axis, angle)
            
            // Update orientation
            currentOrientation = currentOrientation * deltaRotation
            currentOrientation = currentOrientation.normalized()
        }
        
        // Simple position integration (fallback)
        if (lastWorldAcceleration != null) {
            currentVelocity = currentVelocity + (lastWorldAcceleration!! * dt)
            val driftCorrection = 0.99f
            currentVelocity = currentVelocity * driftCorrection
            currentPosition = currentPosition + (currentVelocity * dt)
        }
    }
    
    /**
     * Simple Kalman filter implementation in Kotlin
     */
    private fun applyKotlinKalmanFilter(measurement: Vector3, dt: Float): Vector3 {
        // Simplified Kalman filter (just smoothing for now)
        val alpha = 0.7f
        val filtered = Vector3(
            alpha * lastAcceleration.x + (1 - alpha) * measurement.x,
            alpha * lastAcceleration.y + (1 - alpha) * measurement.y,
            alpha * lastAcceleration.z + (1 - alpha) * measurement.z
        )
        return filtered
    }
    
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Log accuracy changes
    }
    
    /**
     * Get current position
     */
    fun getCurrentPosition(): Vector3 = currentPosition
    
    /**
     * Get current orientation
     */
    fun getCurrentOrientation(): Quaternion = currentOrientation
    
    /**
     * Get current velocity
     */
    fun getCurrentVelocity(): Vector3 = currentVelocity
    
    /**
     * Check if sensors are available
     */
    fun areSensorsAvailable(): Boolean {
        return accelerometerSensor != null && gyroscopeSensor != null
    }
    
    // Native methods
    private external fun nativeKalmanFilter(
        accelX: Float, accelY: Float, accelZ: Float,
        gyroX: Float, gyroY: Float, gyroZ: Float,
        dt: Float
    ): FloatArray
    private external fun nativeGetPosition(): FloatArray
    private external fun nativeGetOrientation(): FloatArray
    private external fun nativeResetFilter()
    
    companion object {
        private const val TAG = "IMUFusion"
        
        init {
            try {
                System.loadLibrary("emfield_native")
            } catch (e: UnsatisfiedLinkError) {
                Log.w(TAG, "Native library not available")
            }
        }
    }
}
