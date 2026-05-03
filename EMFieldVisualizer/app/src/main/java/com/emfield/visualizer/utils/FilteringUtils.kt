package com.emfield.visualizer.utils

import com.emfield.visualizer.data.Vector3

/**
 * Filtering utilities for sensor data processing
 */
object FilteringUtils {
    
    /**
     * Moving average filter
     */
    class MovingAverageFilter(private val windowSize: Int) {
        private val buffer = mutableListOf<Float>()
        
        fun filter(value: Float): Float {
            buffer.add(value)
            if (buffer.size > windowSize) {
                buffer.removeAt(0)
            }
            return buffer.average().toFloat()
        }
        
        fun reset() {
            buffer.clear()
        }
    }
    
    /**
     * Exponential moving average filter
     */
    class ExponentialMovingAverageFilter(private val alpha: Float) {
        private var average: Float? = null
        
        fun filter(value: Float): Float {
            average = if (average == null) {
                value
            } else {
                alpha * average!! + (1 - alpha) * value
            }
            return average!!
        }
        
        fun reset() {
            average = null
        }
    }
    
    /**
     * Median filter for removing spikes
     */
    class MedianFilter(private val windowSize: Int) {
        private val buffer = mutableListOf<Float>()
        
        fun filter(value: Float): Float {
            buffer.add(value)
            if (buffer.size > windowSize) {
                buffer.removeAt(0)
            }
            return buffer.sorted()[buffer.size / 2]
        }
        
        fun reset() {
            buffer.clear()
        }
    }
    
    /**
     * Remove outliers using standard deviation
     */
    fun removeOutliers(values: List<Float>, threshold: Float = 2f): List<Float> {
        if (values.isEmpty()) return values
        
        val mean = values.average().toFloat()
        val variance = values.map { (it - mean) * (it - mean) }.average().toFloat()
        val stdDev = kotlin.math.sqrt(variance)
        
        return values.filter {
            kotlin.math.abs(it - mean) <= threshold * stdDev
        }
    }
    
    /**
     * Apply Savitzky-Golay filter (5-point smoothing)
     */
    fun savitzkyGolay5Point(values: List<Float>): List<Float> {
        if (values.size < 5) return values
        
        val result = mutableListOf<Float>()
        val coefficients = floatArrayOf(-3f, 12f, 17f, 12f, -3f)
        val divisor = 35f
        
        // First two points (use simpler smoothing)
        result.add(values[0])
        result.add(values[1])
        
        // Middle points
        for (i in 2 until values.size - 2) {
            var sum = 0f
            for (j in -2..2) {
                sum += coefficients[j + 2] * values[i + j]
            }
            result.add(sum / divisor)
        }
        
        // Last two points
        result.add(values[values.size - 2])
        result.add(values[values.size - 1])
        
        return result
    }
}
