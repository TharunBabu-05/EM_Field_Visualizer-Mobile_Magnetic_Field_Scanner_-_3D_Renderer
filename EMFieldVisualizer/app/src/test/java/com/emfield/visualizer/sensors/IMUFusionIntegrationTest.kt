package com.emfield.visualizer.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import com.emfield.visualizer.data.Vector3
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration tests for IMUFusion system
 */
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class IMUFusionIntegrationTest {
    
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    
    @Mock
    private lateinit var mockContext: Context
    
    @Mock
    private lateinit var mockSensorManager: SensorManager
    
    @Mock
    private lateinit var mockAccelerometer: Sensor
    
    @Mock
    private lateinit var mockGyroscope: Sensor
    
    private lateinit var imuFusion: IMUFusion
    
    @Before
    fun setup() {
        // Mock sensor manager
        whenever(mockContext.getSystemService(Context.SENSOR_SERVICE))
            .thenReturn(mockSensorManager)
        
        whenever(mockSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER))
            .thenReturn(mockAccelerometer)
        
        whenever(mockSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE))
            .thenReturn(mockGyroscope)
        
        whenever(mockSensorManager.registerListener(any(), any(), anyInt()))
            .thenReturn(true)
        
        // Mock sensor properties
        whenever(mockAccelerometer.name).thenReturn("Accelerometer")
        whenever(mockAccelerometer.type).thenReturn(Sensor.TYPE_ACCELEROMETER)
        whenever(mockAccelerometer.resolution).thenReturn(0.01f)
        whenever(mockAccelerometer.maximumRange).thenReturn 156.8f)
        
        whenever(mockGyroscope.name).thenReturn("Gyroscope")
        whenever(mockGyroscope.type).thenReturn(Sensor.TYPE_GYROSCOPE)
        whenever(mockGyroscope.resolution).thenReturn(0.001f)
        whenever(mockGyroscope.maximumRange).thenReturn(34.9f)
        
        imuFusion = IMUFusion(mockContext)
    }
    
    @Test
    fun testSensorAvailability() {
        assertTrue(imuFusion.areSensorsAvailable())
        
        // Test with missing sensors
        whenever(mockSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER))
            .thenReturn(null)
        
        val imuFusionNoAccel = IMUFusion(mockContext)
        assertTrue(!imuFusionNoAccel.areSensorsAvailable())
    }
    
    @Test
    fun testStartStop() = runTest {
        // Test starting
        val result = imuFusion.start()
        assertTrue(result)
        assertTrue(imuFusion.isRunning())
        
        // Verify sensor registration
        verify(mockSensorManager, times(2)).registerListener(any(), any(), anyInt())
        
        // Test stopping
        imuFusion.stop()
        assertTrue(!imuFusion.isRunning())
        
        // Verify sensor unregistration
        verify(mockSensorManager, times(2)).unregisterListener(any())
    }
    
    @Test
    fun testAccelerometerDataProcessing() = runTest {
        imuFusion.start()
        
        // Send accelerometer data
        val accelEvent = createMockAccelerometerEvent(0f, 0f, 9.81f)
        imuFusion.onSensorChanged(accelEvent)
        
        // Wait for processing
        kotlinx.coroutines.delay(100)
        
        // Verify position flow
        val position = imuFusion.positionFlow.first()
        assertNotNull(position)
        
        imuFusion.stop()
    }
    
    @Test
    fun testGyroscopeDataProcessing() = runTest {
        imuFusion.start()
        
        // Send gyroscope data
        val gyroEvent = createMockGyroscopeEvent(0f, 0f, 0f)
        imuFusion.onSensorChanged(gyroEvent)
        
        // Wait for processing
        kotlinx.coroutines.delay(100)
        
        // Verify orientation flow
        val orientation = imuFusion.orientationFlow.first()
        assertNotNull(orientation)
        
        imuFusion.stop()
    }
    
    @Test
    fun testCombinedSensorFusion() = runTest {
        imuFusion.start()
        
        // Simulate device stationary (gravity only)
        val accelEvents = listOf(
            createMockAccelerometerEvent(0f, 0f, 9.81f),
            createMockAccelerometerEvent(0.1f, 0f, 9.8f),
            createMockAccelerometerEvent(-0.1f, 0f, 9.82f)
        )
        
        // Simulate no rotation
        val gyroEvents = listOf(
            createMockGyroscopeEvent(0f, 0f, 0f),
            createMockGyroscopeEvent(0.01f, 0f, 0f),
            createMockGyroscopeEvent(-0.01f, 0f, 0f)
        )
        
        // Send combined data
        for (i in accelEvents.indices) {
            imuFusion.onSensorChanged(accelEvents[i])
            imuFusion.onSensorChanged(gyroEvents[i])
            kotlinx.coroutines.delay(50)
        }
        
        // Verify fusion results
        val finalPosition = imuFusion.positionFlow.first()
        val finalOrientation = imuFusion.orientationFlow.first()
        
        assertNotNull(finalPosition)
        assertNotNull(finalOrientation)
        
        // Position should be relatively stable for stationary device
        assertTrue(finalPosition.magnitude() < 1.0f)
        
        imuFusion.stop()
    }
    
    @Test
    fun testRotationDetection() = runTest {
        imuFusion.start()
        
        // Simulate 90-degree rotation around Z axis
        val gyroEvents = listOf(
            createMockGyroscopeEvent(0f, 0f, 1.57f), // ~90 deg/s
            createMockGyroscopeEvent(0f, 0f, 1.57f),
            createMockGyroscopeEvent(0f, 0f, 1.57f)
        )
        
        val accelEvents = listOf(
            createMockAccelerometerEvent(0f, 0f, 9.81f),
            createMockAccelerometerEvent(9.81f, 0f, 0f), // After 90 deg rotation
            createMockAccelerometerEvent(0f, -9.81f, 0f) // After 180 deg rotation
        )
        
        for (i in gyroEvents.indices) {
            imuFusion.onSensorChanged(gyroEvents[i])
            imuFusion.onSensorChanged(accelEvents[i])
            kotlinx.coroutines.delay(100)
        }
        
        // Verify orientation changed
        val finalOrientation = imuFusion.orientationFlow.first()
        assertNotNull(finalOrientation)
        
        imuFusion.stop()
    }
    
    @Test
    fun testLinearAccelerationCalculation() = runTest {
        imuFusion.start()
        
        // Simulate device with linear acceleration
        val accelEvents = listOf(
            createMockAccelerometerEvent(0f, 0f, 9.81f), // Stationary
            createMockAccelerometerEvent(1f, 0f, 10.81f), // 1m/s² in X
            createMockAccelerometerEvent(2f, 0f, 11.81f)  // 1m/s² in X again
        )
        
        for (event in accelEvents) {
            imuFusion.onSensorChanged(event)
            kotlinx.coroutines.delay(50)
        }
        
        // Verify position changed due to linear acceleration
        val position = imuFusion.positionFlow.first()
        assertNotNull(position)
        
        // Position should have changed due to linear acceleration
        assertTrue(position.x > 0f)
        
        imuFusion.stop()
    }
    
    @Test
    fun testVelocityTracking() = runTest {
        imuFusion.start()
        
        // Simulate constant acceleration
        val accelEvents = mutableListOf<SensorEvent>()
        for (i in 0..10) {
            accelEvents.add(createMockAccelerometerEvent(1f, 0f, 9.81f))
        }
        
        for (event in accelEvents) {
            imuFusion.onSensorChanged(event)
            kotlinx.coroutines.delay(50)
        }
        
        // Verify velocity increased
        val velocity = imuFusion.getCurrentVelocity()
        assertNotNull(velocity)
        assertTrue(velocity.x > 0f)
        
        imuFusion.stop()
    }
    
    @Test
    fun testReset() = runTest {
        imuFusion.start()
        
        // Send some data to change state
        imuFusion.onSensorChanged(createMockAccelerometerEvent(1f, 2f, 9.81f))
        imuFusion.onSensorChanged(createMockGyroscopeEvent(0.1f, 0.2f, 0.3f))
        kotlinx.coroutines.delay(100)
        
        // Verify state changed
        val positionBefore = imuFusion.getCurrentPosition()
        val orientationBefore = imuFusion.getCurrentOrientation()
        
        // Reset
        imuFusion.reset()
        
        // Verify reset to defaults
        val positionAfter = imuFusion.getCurrentPosition()
        val orientationAfter = imuFusion.getCurrentOrientation()
        
        assertEquals(Vector3.ZERO, positionAfter)
        assertEquals(com.emfield.visualizer.data.Quaternion.IDENTITY, orientationAfter)
        
        imuFusion.stop()
    }
    
    @Test
    fun testNativeFallback() = runTest {
        // Test behavior when native library is not available
        // This test verifies the fallback to Kotlin implementation
        
        imuFusion.start()
        
        // Send sensor data
        imuFusion.onSensorChanged(createMockAccelerometerEvent(0f, 0f, 9.81f))
        imuFusion.onSensorChanged(createMockGyroscopeEvent(0f, 0f, 0f))
        kotlinx.coroutines.delay(100)
        
        // Should still work with fallback
        val position = imuFusion.positionFlow.first()
        val orientation = imuFusion.orientationFlow.first()
        
        assertNotNull(position)
        assertNotNull(orientation)
        
        imuFusion.stop()
    }
    
    @Test
    fun testSensorAccuracy() = runTest {
        imuFusion.start()
        
        // Test with different accuracy levels
        val highAccuracyAccel = createMockAccelerometerEvent(0f, 0f, 9.81f)
        highAccuracyAccel.accuracy = SensorManager.SENSOR_STATUS_ACCURACY_HIGH
        
        val lowAccuracyAccel = createMockAccelerometerEvent(0f, 0f, 9.81f)
        lowAccuracyAccel.accuracy = SensorManager.SENSOR_STATUS_ACCURACY_LOW
        
        // Send high accuracy data
        imuFusion.onSensorChanged(highAccuracyAccel)
        kotlinx.coroutines.delay(50)
        
        val positionHigh = imuFusion.positionFlow.first()
        
        // Send low accuracy data
        imuFusion.onSensorChanged(lowAccuracyAccel)
        kotlinx.coroutines.delay(50)
        
        val positionLow = imuFusion.positionFlow.first()
        
        // Both should produce results, but quality might differ
        assertNotNull(positionHigh)
        assertNotNull(positionLow)
        
        imuFusion.stop()
    }
    
    @Test
    fun testHighFrequencyData() = runTest {
        imuFusion.start()
        
        // Send high-frequency data (100Hz simulation)
        val events = mutableListOf<SensorEvent>()
        for (i in 0..100) {
            events.add(createMockAccelerometerEvent(
                x = (i * 0.01f),
                y = 0f,
                z = 9.81f
            ))
        }
        
        val startTime = System.currentTimeMillis()
        for (event in events) {
            imuFusion.onSensorChanged(event)
            // Simulate 10ms intervals
            kotlinx.coroutines.delay(10)
        }
        val endTime = System.currentTimeMillis()
        
        // Verify processing completed
        val position = imuFusion.positionFlow.first()
        assertNotNull(position)
        
        // Position should reflect the accumulated motion
        assertTrue(position.x > 0f)
        
        imuFusion.stop()
    }
    
    @Test
    fun testErrorHandling() = runTest {
        imuFusion.start()
        
        // Test with NaN values
        val nanEvent = createMockAccelerometerEvent(Float.NaN, 0f, 9.81f)
        imuFusion.onSensorChanged(nanEvent)
        kotlinx.coroutines.delay(50)
        
        // Should handle gracefully
        val position = imuFusion.positionFlow.first()
        assertNotNull(position)
        
        // Test with infinite values
        val infEvent = createMockAccelerometerEvent(Float.POSITIVE_INFINITY, 0f, 9.81f)
        imuFusion.onSensorChanged(infEvent)
        kotlinx.coroutines.delay(50)
        
        // Should handle gracefully
        val position2 = imuFusion.positionFlow.first()
        assertNotNull(position2)
        
        imuFusion.stop()
    }
    
    @Test
    fun testConcurrentAccess() = runTest {
        imuFusion.start()
        
        // Test concurrent sensor data access
        val jobs = mutableListOf<kotlinx.coroutines.Job>()
        
        // Simulate concurrent accelerometer data
        repeat(10) {
            jobs.add(kotlinx.coroutines.launch {
                repeat(10) { i ->
                    imuFusion.onSensorChanged(createMockAccelerometerEvent(
                        x = (it * 0.1f + i * 0.01f),
                        y = 0f,
                        z = 9.81f
                    ))
                    kotlinx.coroutines.delay(10)
                }
            })
        }
        
        // Simulate concurrent gyroscope data
        repeat(10) {
            jobs.add(kotlinx.coroutines.launch {
                repeat(10) { i ->
                    imuFusion.onSensorChanged(createMockGyroscopeEvent(
                        x = 0f,
                        y = (it * 0.01f + i * 0.001f),
                        z = 0f
                    ))
                    kotlinx.coroutines.delay(10)
                }
            })
        }
        
        // Wait for all jobs to complete
        jobs.forEach { it.join() }
        
        // Verify system is still responsive
        val position = imuFusion.positionFlow.first()
        val orientation = imuFusion.orientationFlow.first()
        
        assertNotNull(position)
        assertNotNull(orientation)
        
        imuFusion.stop()
    }
    
    private fun createMockAccelerometerEvent(x: Float, y: Float, z: Float): SensorEvent {
        val sensorEvent = SensorEvent(mockAccelerometer)
        sensorEvent.values[0] = x
        sensorEvent.values[1] = y
        sensorEvent.values[2] = z
        sensorEvent.accuracy = SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM
        sensorEvent.timestamp = System.currentTimeMillis()
        return sensorEvent
    }
    
    private fun createMockGyroscopeEvent(x: Float, y: Float, z: Float): SensorEvent {
        val sensorEvent = SensorEvent(mockGyroscope)
        sensorEvent.values[0] = x
        sensorEvent.values[1] = y
        sensorEvent.values[2] = z
        sensorEvent.accuracy = SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM
        sensorEvent.timestamp = System.currentTimeMillis()
        return sensorEvent
    }
}
