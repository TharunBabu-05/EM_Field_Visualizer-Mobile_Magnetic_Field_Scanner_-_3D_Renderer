package com.emfield.visualizer.data

import android.content.Context
import android.content.SharedPreferences
import com.emfield.visualizer.sensors.MagnetometerManager
import com.emfield.visualizer.sensors.SpatialMapper
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Implementation of SensorRepository
 * Manages sensor data collection and storage
 */
class SensorRepositoryImpl(
    private val context: Context,
    private val magnetometerManager: MagnetometerManager,
    private val spatialMapper: SpatialMapper
) : SensorRepository {
    
    private val preferences: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    
    private val gson = Gson()
    
    override val magneticFieldFlow: Flow<MagneticFieldData>
        get() = magnetometerManager.magneticFieldFlow
    
    override val calibrationData: CalibrationData
        get() = _currentCalibration
    
    private var _currentCalibration: CalibrationData = CalibrationData()
    
    init {
        // Load saved calibration
        loadCalibrationSync()
    }
    
    override suspend fun startCollection() {
        magnetometerManager.start()
    }
    
    override suspend fun stopCollection() {
        magnetometerManager.stop()
    }
    
    override suspend fun getAllDataPoints(): List<MagneticFieldData> {
        return spatialMapper.getAllDataPoints()
    }
    
    override suspend fun clearData() {
        spatialMapper.clear()
    }
    
    override suspend fun saveCalibration(calibration: CalibrationData) {
        _currentCalibration = calibration
        
        // Serialize to JSON
        val json = gson.toJson(calibration)
        preferences.edit()
            .putString(KEY_CALIBRATION, json)
            .putLong(KEY_CALIBRATION_TIMESTAMP, calibration.timestamp)
            .putBoolean(KEY_CALIBRATION_VALID, calibration.isValid)
            .apply()
        
        // Apply to magnetometer
        magnetometerManager.setCalibration(calibration)
    }
    
    override suspend fun loadCalibration(): CalibrationData? {
        return loadCalibrationSync()
    }
    
    private fun loadCalibrationSync(): CalibrationData? {
        val json = preferences.getString(KEY_CALIBRATION, null) ?: return null
        
        return try {
            val calibration = gson.fromJson(json, CalibrationData::class.java)
            _currentCalibration = calibration
            magnetometerManager.setCalibration(calibration)
            calibration
        } catch (e: Exception) {
            null
        }
    }
    
    companion object {
        private const val PREFS_NAME = "em_field_prefs"
        private const val KEY_CALIBRATION = "calibration_data"
        private const val KEY_CALIBRATION_TIMESTAMP = "calibration_timestamp"
        private const val KEY_CALIBRATION_VALID = "calibration_valid"
    }
}
