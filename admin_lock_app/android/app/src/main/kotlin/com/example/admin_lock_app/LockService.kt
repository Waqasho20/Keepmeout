package com.example.admin_lock_app

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class LockService : Service() {
    private var wakeLock: PowerManager.WakeLock? = null
    private var lockJob: Job? = null
    private var duration: Int = 0
    private var password: String = ""
    
    companion object {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "LOCK_SERVICE_CHANNEL"
        const val ACTION_UNLOCK = "ACTION_UNLOCK"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        duration = intent?.getIntExtra("duration", 5) ?: 5
        password = intent?.getStringExtra("password") ?: ""
        
        when (intent?.action) {
            ACTION_UNLOCK -> {
                stopLockService()
                return START_NOT_STICKY
            }
            else -> {
                startLockTimer()
            }
        }
        
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
        
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Lock Service",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Device lock service notification"
            setSound(null, null)
            enableVibration(false)
        }
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val unlockIntent = Intent(this, LockService::class.java).apply {
            action = ACTION_UNLOCK
        }
        val unlockPendingIntent = PendingIntent.getService(
            this, 0, unlockIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val mainIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val mainPendingIntent = PendingIntent.getActivity(
            this, 0, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Device Locked")
            .setContentText("Device is locked for $duration minutes")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SYSTEM)
            .setContentIntent(mainPendingIntent)
            .addAction(
                android.R.drawable.ic_lock_lock,
                "Admin Unlock",
                unlockPendingIntent
            )
            .build()
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "AdminLockApp::LockWakeLock"
        )
        wakeLock?.acquire(duration * 60 * 1000L + 10000L) // Duration + 10 seconds buffer
    }

    private fun startLockTimer() {
        lockJob = CoroutineScope(Dispatchers.Main).launch {
            var remainingSeconds = duration * 60
            
            while (remainingSeconds > 0) {
                delay(1000)
                remainingSeconds--
                updateNotification(remainingSeconds)
            }
            
            // Timer completed, unlock device
            stopLockService()
        }
    }

    private fun updateNotification(remainingSeconds: Int) {
        val minutes = remainingSeconds / 60
        val seconds = remainingSeconds % 60
        val timeText = String.format("%02d:%02d", minutes, seconds)
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Device Locked")
            .setContentText("Time remaining: $timeText")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_SYSTEM)
            .build()
            
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun stopLockService() {
        lockJob?.cancel()
        
        // Send broadcast to unlock the app
        val unlockIntent = Intent("com.example.admin_lock_app.UNLOCK")
        sendBroadcast(unlockIntent)
        
        // Navigate back to admin screen
        val mainIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("unlock", true)
        }
        startActivity(mainIntent)
        
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        lockJob?.cancel()
        wakeLock?.release()
    }
}

