package com.emfield.visualizer.sensors

import android.util.Log
import com.emfield.visualizer.data.MagneticFieldData
import com.emfield.visualizer.data.Vector3
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Maps magnetic field measurements to 3D spatial coordinates
 * Combines magnetometer data with IMU position tracking
 */
class SpatialMapper {
    
    // Data storage
    private val dataPoints = mutableListOf<MagneticFieldData>()
    private val spatialGrid = mutableMapOf<GridCell, MutableList<MagneticFieldData>>()
    
    // Grid resolution (meters)
    private var gridResolution = 0.1f  // 10cm cells
    
    // Flow for data point updates
    private val _dataPointCount = MutableStateFlow(0)
    val dataPointCount: StateFlow<Int> = _dataPointCount.asStateFlow()
    
    /**
     * Add a new data point with position from IMU
     */
    fun addDataPoint(fieldData: MagneticFieldData, position: Vector3) {
        val dataWithPosition = fieldData.copy(position = position)
        
        synchronized(dataPoints) {
            dataPoints.add(dataWithPosition)
            
            // Add to spatial grid for efficient querying
            val cell = positionToGridCell(position)
            spatialGrid.getOrPut(cell) { mutableListOf() }.add(dataWithPosition)
            
            _dataPointCount.value = dataPoints.size
        }
        
        if (dataPoints.size % 100 == 0) {
            Log.d(TAG, "Total data points: ${dataPoints.size}")
        }
    }
    
    /**
     * Get all collected data points
     */
    fun getAllDataPoints(): List<MagneticFieldData> {
        synchronized(dataPoints) {
            return dataPoints.toList()
        }
    }
    
    /**
     * Get data points within a region
     */
    fun getDataPointsInRegion(
        center: Vector3,
        radius: Float
    ): List<MagneticFieldData> {
        synchronized(dataPoints) {
            return dataPoints.filter { data ->
                val distance = (data.position - center).magnitude()
                distance <= radius
            }
        }
    }
    
    /**
     * Get data points in a specific grid cell
     */
    fun getDataPointsInCell(position: Vector3): List<MagneticFieldData> {
        val cell = positionToGridCell(position)
        synchronized(spatialGrid) {
            return spatialGrid[cell]?.toList() ?: emptyList()
        }
    }
    
    /**
     * Get interpolated field value at a position
     */
    fun getInterpolatedField(position: Vector3, radius: Float = 0.5f): Vector3? {
        val nearbyPoints = getDataPointsInRegion(position, radius)
        
        if (nearbyPoints.isEmpty()) {
            return null
        }
        
        // Inverse distance weighted interpolation
        var weightedSum = Vector3.ZERO
        var totalWeight = 0f
        
        for (point in nearbyPoints) {
            val distance = (point.position - position).magnitude()
            val weight = if (distance < 0.001f) {
                // Very close, use the point directly
                return point.field
            } else {
                1f / (distance * distance)
            }
            
            weightedSum = weightedSum + (point.field * weight)
            totalWeight += weight
        }
        
        return if (totalWeight > 0f) {
            weightedSum / totalWeight
        } else {
            null
        }
    }
    
    /**
     * Get field gradient at a position (for particle flow)
     */
    fun getFieldGradient(position: Vector3, delta: Float = 0.1f): Vector3? {
        val field = getInterpolatedField(position) ?: return null
        
        val fieldX = getInterpolatedField(position + Vector3(delta, 0f, 0f))
        val fieldY = getInterpolatedField(position + Vector3(0f, delta, 0f))
        val fieldZ = getInterpolatedField(position + Vector3(0f, 0f, delta))
        
        if (fieldX == null || fieldY == null || fieldZ == null) {
            return null
        }
        
        // Compute gradient
        val gradX = (fieldX.magnitude() - field.magnitude()) / delta
        val gradY = (fieldY.magnitude() - field.magnitude()) / delta
        val gradZ = (fieldZ.magnitude() - field.magnitude()) / delta
        
        return Vector3(gradX, gradY, gradZ)
    }
    
    /**
     * Get bounding box of all data
     */
    fun getBoundingBox(): Pair<Vector3, Vector3>? {
        synchronized(dataPoints) {
            if (dataPoints.isEmpty()) return null
            
            var minX = Float.MAX_VALUE
            var minY = Float.MAX_VALUE
            var minZ = Float.MAX_VALUE
            var maxX = Float.MIN_VALUE
            var maxY = Float.MIN_VALUE
            var maxZ = Float.MIN_VALUE
            
            for (point in dataPoints) {
                minX = minOf(minX, point.position.x)
                minY = minOf(minY, point.position.y)
                minZ = minOf(minZ, point.position.z)
                maxX = maxOf(maxX, point.position.x)
                maxY = maxOf(maxY, point.position.y)
                maxZ = maxOf(maxZ, point.position.z)
            }
            
            return Pair(
                Vector3(minX, minY, minZ),
                Vector3(maxX, maxY, maxZ)
            )
        }
    }
    
    /**
     * Get statistics about collected data
     */
    fun getStatistics(): SpatialStatistics {
        synchronized(dataPoints) {
            if (dataPoints.isEmpty()) {
                return SpatialStatistics(
                    totalPoints = 0,
                    gridCells = 0,
                    minFieldStrength = 0f,
                    maxFieldStrength = 0f,
                    avgFieldStrength = 0f,
                    volume = 0f
                )
            }
            
            var minField = Float.MAX_VALUE
            var maxField = Float.MIN_VALUE
            var sumField = 0f
            
            for (point in dataPoints) {
                val magnitude = point.magnitude()
                minField = minOf(minField, magnitude)
                maxField = maxOf(maxField, magnitude)
                sumField += magnitude
            }
            
            val boundingBox = getBoundingBox()
            val volume = if (boundingBox != null) {
                val (min, max) = boundingBox
                val size = max - min
                size.x * size.y * size.z
            } else {
                0f
            }
            
            return SpatialStatistics(
                totalPoints = dataPoints.size,
                gridCells = spatialGrid.size,
                minFieldStrength = minField,
                maxFieldStrength = maxField,
                avgFieldStrength = sumField / dataPoints.size,
                volume = volume
            )
        }
    }
    
    /**
     * Clear all data
     */
    fun clear() {
        synchronized(dataPoints) {
            dataPoints.clear()
            spatialGrid.clear()
            _dataPointCount.value = 0
        }
        Log.i(TAG, "Spatial data cleared")
    }
    
    /**
     * Set grid resolution
     */
    fun setGridResolution(resolution: Float) {
        require(resolution > 0) { "Grid resolution must be positive" }
        gridResolution = resolution
        
        // Rebuild grid with new resolution
        rebuildGrid()
    }
    
    private fun rebuildGrid() {
        synchronized(spatialGrid) {
            spatialGrid.clear()
            for (point in dataPoints) {
                val cell = positionToGridCell(point.position)
                spatialGrid.getOrPut(cell) { mutableListOf() }.add(point)
            }
        }
        Log.i(TAG, "Grid rebuilt with resolution $gridResolution m")
    }
    
    private fun positionToGridCell(position: Vector3): GridCell {
        return GridCell(
            (position.x / gridResolution).toInt(),
            (position.y / gridResolution).toInt(),
            (position.z / gridResolution).toInt()
        )
    }
    
    companion object {
        private const val TAG = "SpatialMapper"
    }
}

/**
 * Grid cell for spatial indexing
 */
data class GridCell(val x: Int, val y: Int, val z: Int)

/**
 * Statistics about spatial data
 */
data class SpatialStatistics(
    val totalPoints: Int,
    val gridCells: Int,
    val minFieldStrength: Float,
    val maxFieldStrength: Float,
    val avgFieldStrength: Float,
    val volume: Float  // Cubic meters
)
