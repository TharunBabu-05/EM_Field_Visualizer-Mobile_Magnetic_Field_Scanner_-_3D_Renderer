package com.emfield.visualizer.data

/**
 * Statistics for high-frequency buffer performance
 */
data class BufferStats(
    val bufferSize: Int,
    val flushInterval: Long,
    val lastFlushTime: Long
)
