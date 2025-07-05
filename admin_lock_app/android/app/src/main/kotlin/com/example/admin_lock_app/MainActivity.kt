package com.example.admin_lock_app

import android.app.ActivityManager
import android.app.admin.DevicePolicyManager
import android.content.*
import android.os.Bundle
import android.view.KeyEvent
import android.view.WindowManager
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity : FlutterActivity() {
    private val CHANNEL = "admin_lock_app/lock"
    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var adminComponent: ComponentName
    private var isLocked = false
    private lateinit var unlockReceiver: BroadcastReceiver
    private lateinit var sharedPrefs: SharedPreferences

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        
        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminComponent = ComponentName(this, AdminReceiver::class.java)
        sharedPrefs = getSharedPreferences("AdminLockApp", Context.MODE_PRIVATE)
        
        setupUnlockReceiver()
        
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL).setMethodCallHandler { call, result ->
            when (call.method) {
                "startLock" -> {
                    val duration = call.argument<Int>("duration") ?: 5
                    val password = call.argument<String>("password") ?: ""
                    startLock(duration, password)
                    result.success(null)
                }
                "enableKioskMode" -> {
                    enableKioskMode()
                    result.success(null)
                }
                "disableKioskMode" -> {
                    disableKioskMode()
                    result.success(null)
                }
                "isDeviceAdmin" -> {
                    result.success(isDeviceAdmin())
                }
                "requestDeviceAdmin" -> {
                    requestDeviceAdmin()
                    result.success(null)
                }
                "lockDevice" -> {
                    lockDevice()
                    result.success(null)
                }
                "startForegroundService" -> {
                    startForegroundService()
                    result.success(null)
                }
                "stopForegroundService" -> {
                    stopForegroundService()
                    result.success(null)
                }
                "enableScreenPinning" -> {
                    enableScreenPinning()
                    result.success(null)
                }
                "disableScreenPinning" -> {
                    disableScreenPinning()
                    result.success(null)
                }
                else -> {
                    result.notImplemented()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check if this is an auto-lock from boot
        val autoLock = intent.getBooleanExtra("auto_lock", false)
        if (autoLock) {
            val duration = intent.getIntExtra("duration", 5)
            val password = intent.getStringExtra("password") ?: ""
            startLock(duration, password)
        }
        
        // Request device admin if not already granted
        if (!isDeviceAdmin()) {
            requestDeviceAdmin()
        }
    }

    private fun setupUnlockReceiver() {
        unlockReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "com.example.admin_lock_app.UNLOCK") {
                    disableKioskMode()
                }
            }
        }
        
        val filter = IntentFilter("com.example.admin_lock_app.UNLOCK")
        registerReceiver(unlockReceiver, filter)
    }

    private fun startLock(duration: Int, password: String) {
        isLocked = true
        
        // Save lock state to SharedPreferences
        val lockEndTime = System.currentTimeMillis() + (duration * 60 * 1000)
        sharedPrefs.edit().apply {
            putBoolean("is_locked", true)
            putLong("lock_end_time", lockEndTime)
            putString("admin_password", password)
            apply()
        }
        
        // Start foreground service
        val serviceIntent = Intent(this, LockService::class.java).apply {
            putExtra("duration", duration)
            putExtra("password", password)
        }
        startForegroundService(serviceIntent)
        
        // Enable kiosk mode
        enableKioskMode()
    }

    private fun enableKioskMode() {
        isLocked = true
        
        // Set flags to prevent user from leaving the app
        window.addFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        
        // Start lock task mode (screen pinning)
        if (isDeviceAdmin()) {
            try {
                startLockTask()
            } catch (e: Exception) {
                // Fallback if lock task mode fails
                enableScreenPinning()
            }
        }
    }

    private fun disableKioskMode() {
        isLocked = false
        
        // Clear lock state from SharedPreferences
        sharedPrefs.edit().apply {
            putBoolean("is_locked", false)
            putLong("lock_end_time", 0)
            remove("admin_password")
            apply()
        }
        
        // Clear flags
        window.clearFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        
        // Stop lock task mode
        try {
            stopLockTask()
        } catch (e: Exception) {
            // Handle exception
        }
        
        // Stop foreground service
        stopForegroundService()
    }

    private fun isDeviceAdmin(): Boolean {
        return devicePolicyManager.isAdminActive(adminComponent)
    }

    private fun requestDeviceAdmin() {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
            putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
            putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, 
                "This app requires device admin privileges to lock the device securely.")
        }
        startActivity(intent)
    }

    private fun lockDevice() {
        if (isDeviceAdmin()) {
            devicePolicyManager.lockNow()
        }
    }

    private fun startForegroundService() {
        val serviceIntent = Intent(this, LockService::class.java)
        startForegroundService(serviceIntent)
    }

    private fun stopForegroundService() {
        val serviceIntent = Intent(this, LockService::class.java)
        stopService(serviceIntent)
    }

    private fun enableScreenPinning() {
        if (isDeviceAdmin()) {
            try {
                startLockTask()
            } catch (e: Exception) {
                // Handle exception
            }
        }
    }

    private fun disableScreenPinning() {
        try {
            stopLockTask()
        } catch (e: Exception) {
            // Handle exception
        }
    }

    // Override key events to prevent back/home button when locked
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (isLocked) {
            when (keyCode) {
                KeyEvent.KEYCODE_BACK,
                KeyEvent.KEYCODE_HOME,
                KeyEvent.KEYCODE_MENU,
                KeyEvent.KEYCODE_APP_SWITCH -> {
                    return true // Block these keys
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onBackPressed() {
        if (isLocked) {
            // Do nothing - prevent back navigation when locked
            return
        }
        super.onBackPressed()
    }

    override fun onPause() {
        super.onPause()
        if (isLocked) {
            // Bring app back to foreground if locked
            val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            activityManager.moveTaskToFront(taskId, 0)
        }
    }

    override fun onStop() {
        super.onStop()
        if (isLocked) {
            // Restart activity if locked
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            startActivity(intent)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(unlockReceiver)
        } catch (e: Exception) {
            // Receiver might not be registered
        }
    }
}

