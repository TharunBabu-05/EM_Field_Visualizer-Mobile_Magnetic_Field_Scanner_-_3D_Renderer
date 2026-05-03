package com.emfield.visualizer.sensors

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.emfield.visualizer.R
import com.emfield.visualizer.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * Foreground service for continuous sensor monitoring
 * Keeps sensors active even when app is in background
 */
class SensorMonitorService : Service() {
    
    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    private lateinit var magnetometerManager: MagnetometerManager
    private lateinit var imuFusion: IMUFusion
    private lateinit var spatialMapper: SpatialMapper
    
    private var wakeLock: PowerManager.WakeLock? = null
    private var isScanning = false
    
    inner class LocalBinder : Binder() {
        fun getService(): SensorMonitorService = this@SensorMonitorService
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Service created")
        
        // Initialize sensor managers
        magnetometerManager = MagnetometerManager(this)
        imuFusion = IMUFusion(this)
        spatialMapper = SpatialMapper()
        
        // Create notification channel
        createNotificationChannel()
        
        // Acquire wake lock to keep CPU running
        acquireWakeLock()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "Service started")
        
        // Start as foreground service
        val notification = createNotification("EM Field Scanner", "Ready to scan")
        startForeground(NOTIFICATION_ID, notification)
        
        return START_STICKY
    }
    
    override fun onBind(intent: Intent): IBinder {
        return binder
    }
    
    /**
     * Start scanning
     */
    fun startScanning(): Boolean {
        if (isScanning) {
            Log.w(TAG, "Already scanning")
            return true
        }
        
        // Start sensors
        val magnetometerStarted = magnetometerManager.start()
        val imuStarted = imuFusion.start()
        
        if (!magnetometerStarted || !imuStarted) {
            Log.e(TAG, "Failed to start sensors")
            magnetometerManager.stop()
            imuFusion.stop()
            return false
        }
        
        isScanning = true
        
        // Combine magnetometer and IMU data
        serviceScope.launch {
            combine(
                magnetometerManager.magneticFieldFlow,
                imuFusion.positionFlow
            ) { fieldData, position ->
                Pair(fieldData, position)
            }.collect { (fieldData, position) ->
                // Add data point with current position
                spatialMapper.addDataPoint(fieldData, position)
                
                // Update notification
                updateNotification(
                    "Scanning...",
                    "${spatialMapper.dataPointCount.value} points collected"
                )
            }
        }
        
        updateNotification("Scanning", "Started")
        Log.i(TAG, "Scanning started")
        
        return true
    }
    
    /**
     * Stop scanning
     */
    fun stopScanning() {
        if (!isScanning) return
        
        magnetometerManager.stop()
        imuFusion.stop()
        isScanning = false
        
        updateNotification("Scan Complete", "${spatialMapper.dataPointCount.value} points collected")
        Log.i(TAG, "Scanning stopped")
    }
    
    /**
     * Reset IMU position
     */
    fun resetPosition() {
        imuFusion.reset()
        Log.i(TAG, "Position reset")
    }
    
    /**
     * Clear collected data
     */
    fun clearData() {
        spatialMapper.clear()
        Log.i(TAG, "Data cleared")
    }
    
    /**
     * Get managers for external access
     */
    fun getMagnetometerManager(): MagnetometerManager = magnetometerManager
    fun getIMUFusion(): IMUFusion = imuFusion
    fun getSpatialMapper(): SpatialMapper = spatialMapper
    
    /**
     * Check if currently scanning
     */
    fun isScanning(): Boolean = isScanning
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "EM Field Scanner",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Continuous electromagnetic field scanning"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(title: String, text: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
    
    private fun updateNotification(title: String, text: String) {
        val notification = createNotification(title, text)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "EMFieldVisualizer::SensorWakeLock"
        )
        wakeLock?.acquire(10 * 60 * 1000L) // 10 minutes
        Log.i(TAG, "Wake lock acquired")
    }
    
    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
                Log.i(TAG, "Wake lock released")
            }
        }
        wakeLock = null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        stopScanning()
        
        magnetometerManager.cleanup()
        imuFusion.stop()
        
        releaseWakeLock()
        serviceScope.cancel()
        
        Log.i(TAG, "Service destroyed")
    }
    
    companion object {
        private const val TAG = "SensorMonitorService"
        private const val CHANNEL_ID = "em_field_scanner"
        private const val NOTIFICATION_ID = 1001
    }
}
