package com.example.admin_lock_app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            "android.intent.action.QUICKBOOT_POWERON" -> {
                handleBootCompleted(context)
            }
        }
    }

    private fun handleBootCompleted(context: Context) {
        val sharedPrefs = context.getSharedPreferences("AdminLockApp", Context.MODE_PRIVATE)
        val isLocked = sharedPrefs.getBoolean("is_locked", false)
        val lockEndTime = sharedPrefs.getLong("lock_end_time", 0)
        
        if (isLocked && System.currentTimeMillis() < lockEndTime) {
            // Device is still supposed to be locked
            val remainingTime = ((lockEndTime - System.currentTimeMillis()) / 1000 / 60).toInt()
            val password = sharedPrefs.getString("admin_password", "") ?: ""
            
            // Start the main activity in locked mode
            val mainIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("auto_lock", true)
                putExtra("duration", remainingTime)
                putExtra("password", password)
            }
            context.startActivity(mainIntent)
            
            // Start the lock service
            val serviceIntent = Intent(context, LockService::class.java).apply {
                putExtra("duration", remainingTime)
                putExtra("password", password)
            }
            context.startForegroundService(serviceIntent)
        } else if (isLocked) {
            // Lock time has expired, clear the lock state
            clearLockState(sharedPrefs)
        }
    }

    private fun clearLockState(sharedPrefs: SharedPreferences) {
        sharedPrefs.edit().apply {
            putBoolean("is_locked", false)
            putLong("lock_end_time", 0)
            remove("admin_password")
            apply()
        }
    }
}

