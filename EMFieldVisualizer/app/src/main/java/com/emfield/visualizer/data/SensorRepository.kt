package com.emfield.visualizer.data

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing sensor data collection and storage
 */
interface SensorRepository {
    /**
     * Flow of real-time magnetic field measurements
     */
    val magneticFieldFlow: Flow<MagneticFieldData>
    
    /**
     * Current calibration state
     */
    val calibrationData: CalibrationData
    
    /**
     * Start collecting sensor data
     */
    suspend fun startCollection()
    
    /**
     * Stop collecting sensor data
     */
    suspend fun stopCollection()
    
    /**
     * Get all collected data points
     */
    suspend fun getAllDataPoints(): List<MagneticFieldData>
    
    /**
     * Clear all collected data
     */
    suspend fun clearData()
    
    /**
     * Save calibration data
     */
    suspend fun saveCalibration(calibration: CalibrationData)
    
    /**
     * Load saved calibration data
     */
    suspend fun loadCalibration(): CalibrationData?
}
