package com.emfield.visualizer.utils

import kotlin.math.exp
import kotlin.math.sqrt

/**
 * Mathematical utility functions for sensor processing
 */
object MathUtils {
    
    /**
     * Low-pass filter for smoothing sensor data
     */
    fun lowPassFilter(current: Float, previous: Float, alpha: Float): Float {
        return alpha * previous + (1 - alpha) * current
    }
    
    /**
     * Calculate Euclidean distance between two 3D points
     */
    fun distance(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float): Float {
        val dx = x2 - x1
        val dy = y2 - y1
        val dz = z2 - z1
        return sqrt(dx * dx + dy * dy + dz * dz)
    }
    
    /**
     * Linear interpolation
     */
    fun lerp(a: Float, b: Float, t: Float): Float {
        return a + (b - a) * t.coerceIn(0f, 1f)
    }
    
    /**
     * Smooth step interpolation
     */
    fun smoothStep(edge0: Float, edge1: Float, x: Float): Float {
        val t = ((x - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
        return t * t * (3 - 2 * t)
    }
    
    /**
     * Gaussian function for weighted averaging
     */
    fun gaussian(x: Float, sigma: Float): Float {
        return exp(-(x * x) / (2 * sigma * sigma)).toFloat()
    }
    
    /**
     * Clamp value between min and max
     */
    fun clamp(value: Float, min: Float, max: Float): Float {
        return value.coerceIn(min, max)
    }
    
    /**
     * Normalize angle to [-180, 180] range
     */
    fun normalizeAngle(angle: Float): Float {
        var normalized = angle % 360f
        if (normalized > 180f) normalized -= 360f
        if (normalized < -180f) normalized += 360f
        return normalized
    }
    
    /**
     * Convert degrees to radians
     */
    fun toRadians(degrees: Float): Float {
        return degrees * (Math.PI.toFloat() / 180f)
    }
    
    /**
     * Convert radians to degrees
     */
    fun toDegrees(radians: Float): Float {
        return radians * (180f / Math.PI.toFloat())
    }
}
