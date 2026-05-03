package com.emfield.visualizer.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import android.util.Log
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
import com.emfield.visualizer.data.MagneticFieldData
import com.emfield.visualizer.data.Vector3
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit tests for MagnetometerManager
 */
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class MagnetometerManagerTest {
    
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    
    @Mock
    private lateinit var mockContext: Context
    
    @Mock
    private lateinit var mockSensorManager: SensorManager
    
    @Mock
    private lateinit var mockMagnetometerSensor: Sensor
    
    @Mock
    private lateinit var mockUncalibratedSensor: Sensor
    
    private lateinit var magnetometerManager: MagnetometerManager
    
    @Before
    fun setup() {
        // Mock sensor manager behavior
        whenever(mockContext.getSystemService(Context.SENSOR_SERVICE))
            .thenReturn(mockSensorManager)
        
        whenever(mockSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED))
            .thenReturn(mockUncalibratedSensor)
        
        whenever(mockSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD))
            .thenReturn(mockMagnetometerSensor)
        
        whenever(mockUncalibratedSensor.name).thenReturn("Uncalibrated Magnetometer")
        whenever(mockUncalibratedSensor.vendor).thenReturn("Test Vendor")
        whenever(mockUncalibratedSensor.type).thenReturn(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED)
        whenever(mockUncalibratedSensor.version).thenReturn(1)
        whenever(mockUncalibratedSensor.resolution).thenReturn(0.1f)
        whenever(mockUncalibratedSensor.maximumRange).thenReturn(2000f)
        whenever(mockUncalibratedSensor.power).thenReturn(0.1f)
        whenever(mockUncalibratedSensor.minDelay).thenReturn(10000)
        
        whenever(mockMagnetometerSensor.name).thenReturn("Magnetometer")
        whenever(mockMagnetometerSensor.vendor).thenReturn("Test Vendor")
        whenever(mockMagnetometerSensor.type).thenReturn(Sensor.TYPE_MAGNETIC_FIELD)
        whenever(mockMagnetometerSensor.version).thenReturn(1)
        whenever(mockMagnetometerSensor.resolution).thenReturn(0.1f)
        whenever(mockMagnetometerSensor.maximumRange).thenReturn(2000f)
        whenever(mockMagnetometerSensor.power).thenReturn(0.1f)
        whenever(mockMagnetometerSensor.minDelay).thenReturn(10000)
        
        whenever(mockSensorManager.registerListener(any(), any(), anyInt()))
            .thenReturn(true)
        
        magnetometerManager = MagnetometerManager(mockContext)
    }
    
    @Test
    fun testSensorAvailability() {
        // Test when both sensors are available
        assertTrue(magnetometerManager.isSensorAvailable())
        
        // Test when no sensors are available
        whenever(mockSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED))
            .thenReturn(null)
        whenever(mockSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD))
            .thenReturn(null)
        
        val managerNoSensors = MagnetometerManager(mockContext)
        assertTrue(managerNoSensors.isSensorAvailable()) // Should fallback to calibrated sensor
    }
    
    @Test
    fun testStartStop() = runTest {
        // Test starting sensor
        val result = magnetometerManager.start()
        assertTrue(result)
        assertTrue(magnetometerManager.isRunning())
        
        // Verify sensor registration
        verify(mockSensorManager).registerListener(any(), any(), anyInt())
        
        // Test stopping sensor
        magnetometerManager.stop()
        assertTrue(!magnetometerManager.isRunning())
        
        // Verify sensor unregistration
        verify(mockSensorManager).unregisterListener(any())
    }
    
    @Test
    fun testHighFrequencyMode() {
        // Test enabling high frequency mode
        magnetometerManager.setHighFrequencyMode(true)
        assertTrue(magnetometerManager.isHighFrequencyModeEnabled())
        
        // Test disabling high frequency mode
        magnetometerManager.setHighFrequencyMode(false)
        assertTrue(!magnetometerManager.isHighFrequencyModeEnabled())
    }
    
    @Test
    fun testAdaptiveSampling() {
        // Test enabling adaptive sampling
        magnetometerManager.setAdaptiveSampling(true)
        assertTrue(magnetometerManager.isAdaptiveSamplingEnabled())
        
        // Test disabling adaptive sampling
        magnetometerManager.setAdaptiveSampling(false)
        assertTrue(!magnetometerManager.isAdaptiveSamplingEnabled())
    }
    
    @Test
    fun testTargetSamplingRate() {
        val targetRate = 200f
        magnetometerManager.setTargetSamplingRate(targetRate)
        assertEquals(targetRate, magnetometerManager.getTargetSamplingRate())
    }
    
    @Test
    fun testCalibration() {
        val calibrationData = com.emfield.visualizer.data.CalibrationData(
            hardIronOffset = Vector3(1f, 2f, 3f),
            softIronMatrix = arrayOf(
                floatArrayOf(1f, 0f, 0f),
                floatArrayOf(0f, 1f, 0f),
                floatArrayOf(0f, 0f, 1f)
            ),
            timestamp = System.currentTimeMillis(),
            isValid = true
        )
        
        magnetometerManager.setCalibration(calibrationData)
        assertNotNull(magnetometerManager.getCalibration())
        assertTrue(magnetometerManager.getCalibration().isValid)
    }
    
    @Test
    fun testSensorEventProcessing() = runTest {
        // Start the sensor
        magnetometerManager.start()
        
        // Create a mock sensor event
        val sensorEvent = createMockSensorEvent(
            x = 10f,
            y = 20f,
            z = 30f,
            accuracy = SensorManager.SENSOR_STATUS_ACCURACY_HIGH
        )
        
        // Simulate sensor event
        magnetometerManager.onSensorChanged(sensorEvent)
        
        // Collect the emitted data
        val emittedData = magnetometerManager.magneticFieldFlow.take(1).first()
        
        // Verify the data
        assertEquals(10f, emittedData.field.x, 0.01f)
        assertEquals(20f, emittedData.field.y, 0.01f)
        assertEquals(30f, emittedData.field.z, 0.01f)
        assertEquals(SensorManager.SENSOR_STATUS_ACCURACY_HIGH, emittedData.accuracy)
        
        magnetometerManager.stop()
    }
    
    @Test
    fun testSensorInfo() {
        val sensorInfo = magnetometerManager.getSensorInfo()
        
        assertNotNull(sensorInfo)
        assertTrue(sensorInfo.contains("Uncalibrated Magnetometer"))
        assertTrue(sensorInfo.contains("Test Vendor"))
        assertTrue(sensorInfo.contains("0.1"))
        assertTrue(sensorInfo.contains("2000.0"))
    }
    
    @Test
    fun testSamplingRate() {
        // Test when not running
        assertEquals(0f, magnetometerManager.getSamplingRate())
        
        // Start sensor and simulate events
        magnetometerManager.start()
        
        val startTime = System.currentTimeMillis()
        magnetometerManager.onSensorChanged(createMockSensorEvent(10f, 20f, 30f))
        Thread.sleep(100)
        magnetometerManager.onSensorChanged(createMockSensorEvent(15f, 25f, 35f))
        
        val samplingRate = magnetometerManager.getSamplingRate()
        assertTrue(samplingRate > 0f)
        
        magnetometerManager.stop()
    }
    
    @Test
    fun testMultipleSensorEvents() = runTest {
        magnetometerManager.start()
        
        val testEvents = listOf(
            createMockSensorEvent(10f, 20f, 30f),
            createMockSensorEvent(15f, 25f, 35f),
            createMockSensorEvent(20f, 30f, 40f)
        )
        
        val collectedData = mutableListOf<MagneticFieldData>()
        
        // Collect multiple events
        magnetometerManager.magneticFieldFlow.take(3).collect { data ->
            collectedData.add(data)
        }
        
        // Simulate events
        testEvents.forEach { event ->
            magnetometerManager.onSensorChanged(event)
        }
        
        // Verify all events were collected
        assertEquals(3, collectedData.size)
        assertEquals(10f, collectedData[0].field.x, 0.01f)
        assertEquals(15f, collectedData[1].field.x, 0.01f)
        assertEquals(20f, collectedData[2].field.x, 0.01f)
        
        magnetometerManager.stop()
    }
    
    @Test
    fun testCalibrationAppliedToSensorData() = runTest {
        // Set up calibration
        val calibrationData = com.emfield.visualizer.data.CalibrationData(
            hardIronOffset = Vector3(5f, 10f, 15f),
            softIronMatrix = arrayOf(
                floatArrayOf(2f, 0f, 0f),
                floatArrayOf(0f, 2f, 0f),
                floatArrayOf(0f, 0f, 2f)
            ),
            timestamp = System.currentTimeMillis(),
            isValid = true
        )
        
        magnetometerManager.setCalibration(calibrationData)
        magnetometerManager.start()
        
        // Send raw sensor data
        val rawEvent = createMockSensorEvent(15f, 30f, 45f)
        magnetometerManager.onSensorChanged(rawEvent)
        
        // Collect calibrated data
        val calibratedData = magnetometerManager.magneticFieldFlow.take(1).first()
        
        // Verify calibration was applied
        // Raw: (15, 30, 45) - Offset: (5, 10, 15) = (10, 20, 30)
        // Scaled by matrix: (20, 40, 60)
        assertEquals(20f, calibratedData.field.x, 0.01f)
        assertEquals(40f, calibratedData.field.y, 0.01f)
        assertEquals(60f, calibratedData.field.z, 0.01f)
        
        magnetometerManager.stop()
    }
    
    @Test
    fun testErrorHandling() {
        // Test starting when already running
        magnetometerManager.start()
        val result = magnetometerManager.start()
        assertTrue(result) // Should return true but not start again
        
        // Test stopping when not running
        magnetometerManager.stop()
        magnetometerManager.stop() // Should not crash
        
        // Test sensor not available
        whenever(mockSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED))
            .thenReturn(null)
        whenever(mockSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD))
            .thenReturn(null)
        
        val managerNoSensors = MagnetometerManager(mockContext)
        val startResult = managerNoSensors.start()
        assertTrue(startResult) // Should handle gracefully
    }
    
    @Test
    fun testNativeLibraryLoading() {
        // Test that native library loading doesn't crash
        // This test verifies the try-catch block in the init block
        val manager = MagnetometerManager(mockContext)
        assertNotNull(manager)
    }
    
    private fun createMockSensorEvent(x: Float, y: Float, z: Float, accuracy: Int = SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM): SensorEvent {
        val sensorEvent = SensorEvent(mockMagnetometerSensor)
        sensorEvent.values[0] = x
        sensorEvent.values[1] = y
        sensorEvent.values[2] = z
        sensorEvent.accuracy = accuracy
        sensorEvent.timestamp = System.currentTimeMillis()
        return sensorEvent
    }
}
