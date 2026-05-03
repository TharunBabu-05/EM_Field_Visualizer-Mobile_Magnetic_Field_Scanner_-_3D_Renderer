package com.emfield.visualizer.data

import kotlin.math.sqrt

/**
 * Represents a magnetic field measurement at a specific point in 3D space
 */
data class MagneticFieldData(
    val timestamp: Long,              // Milliseconds since epoch
    val position: Vector3,            // Position in world space (meters)
    val field: Vector3,               // Magnetic field vector (microTesla)
    val accuracy: Int = 0             // Sensor accuracy (0-3, higher is better)
) {
    /** Field magnitude in microTesla */
    fun magnitude(): Float = field.magnitude()
    
    /** Normalized field direction */
    fun direction(): Vector3 = field.normalized()
}

/**
 * 3D Vector for positions and magnetic field vectors
 */
data class Vector3(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f
) {
    fun magnitude(): Float = sqrt(x * x + y * y + z * z)
    
    fun normalized(): Vector3 {
        val mag = magnitude()
        return if (mag > 0f) Vector3(x / mag, y / mag, z / mag) else this
    }
    
    operator fun plus(other: Vector3) = Vector3(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Vector3) = Vector3(x - other.x, y - other.y, z - other.z)
    operator fun times(scalar: Float) = Vector3(x * scalar, y * scalar, z * scalar)
    operator fun div(scalar: Float) = Vector3(x / scalar, y / scalar, z / scalar)
    
    fun dot(other: Vector3): Float = x * other.x + y * other.y + z * other.z
    
    fun cross(other: Vector3) = Vector3(
        y * other.z - z * other.y,
        z * other.x - x * other.z,
        x * other.y - y * other.x
    )
    
    fun toFloatArray(): FloatArray = floatArrayOf(x, y, z)
    
    companion object {
        val ZERO = Vector3(0f, 0f, 0f)
        val UP = Vector3(0f, 1f, 0f)
        val FORWARD = Vector3(0f, 0f, 1f)
        val RIGHT = Vector3(1f, 0f, 0f)
    }
}

/**
 * Quaternion for rotation representation
 */
data class Quaternion(
    val w: Float = 1f,
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f
) {
    fun magnitude(): Float = sqrt(w * w + x * x + y * y + z * z)
    
    fun normalized(): Quaternion {
        val mag = magnitude()
        return if (mag > 0f) Quaternion(w / mag, x / mag, y / mag, z / mag) else this
    }
    
    operator fun times(other: Quaternion) = Quaternion(
        w * other.w - x * other.x - y * other.y - z * other.z,
        w * other.x + x * other.w + y * other.z - z * other.y,
        w * other.y - x * other.z + y * other.w + z * other.x,
        w * other.z + x * other.y - y * other.x + z * other.w
    )
    
    fun rotateVector(v: Vector3): Vector3 {
        val qv = Quaternion(0f, v.x, v.y, v.z)
        val conjugate = Quaternion(w, -x, -y, -z)
        val result = this * qv * conjugate
        return Vector3(result.x, result.y, result.z)
    }
    
    companion object {
        val IDENTITY = Quaternion(1f, 0f, 0f, 0f)
        
        fun fromAxisAngle(axis: Vector3, angle: Float): Quaternion {
            val halfAngle = angle / 2f
            val s = kotlin.math.sin(halfAngle)
            val normalized = axis.normalized()
            return Quaternion(
                kotlin.math.cos(halfAngle),
                normalized.x * s,
                normalized.y * s,
                normalized.z * s
            )
        }
    }
}

/**
 * Calibration data for magnetometer
 */
data class CalibrationData(
    val hardIronOffset: Vector3 = Vector3.ZERO,     // Bias correction
    val softIronMatrix: Array<FloatArray> = Array(3) { FloatArray(3) { 0f } },  // Scale/skew correction
    val timestamp: Long = System.currentTimeMillis(),
    val isValid: Boolean = false
) {
    fun apply(rawField: Vector3): Vector3 {
        if (!isValid) return rawField
        
        // Subtract hard iron offset
        val corrected = rawField - hardIronOffset
        
        // Apply soft iron matrix
        val x = softIronMatrix[0][0] * corrected.x + softIronMatrix[0][1] * corrected.y + softIronMatrix[0][2] * corrected.z
        val y = softIronMatrix[1][0] * corrected.x + softIronMatrix[1][1] * corrected.y + softIronMatrix[1][2] * corrected.z
        val z = softIronMatrix[2][0] * corrected.x + softIronMatrix[2][1] * corrected.y + softIronMatrix[2][2] * corrected.z
        
        return Vector3(x, y, z)
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as CalibrationData
        if (hardIronOffset != other.hardIronOffset) return false
        if (!softIronMatrix.contentDeepEquals(other.softIronMatrix)) return false
        return true
    }
    
    override fun hashCode(): Int {
        var result = hardIronOffset.hashCode()
        result = 31 * result + softIronMatrix.contentDeepHashCode()
        return result
    }
}
