package com.securelock;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;
import java.util.Set;
import java.util.Calendar;

public class LockReceiver extends BroadcastReceiver {
    private static final String TAG = "LockReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received intent: " + intent.getAction());
        
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        SharedPreferences prefs = context.getSharedPreferences("SecureLockPrefs", Context.MODE_PRIVATE);
        
        String action = intent.getAction();
        String day = intent.getStringExtra("day");
        int hour = intent.getIntExtra("hour", 0);
        int minute = intent.getIntExtra("minute", 0);
        int duration = intent.getIntExtra("duration", 0);
        
        Log.d(TAG, "Action: " + action + ", Day: " + day + ", Hour: " + hour + ", Minute: " + minute);
        Log.d(TAG, "Current time: " + System.currentTimeMillis());
        Log.d(TAG, "Device admin active: " + dpm.isAdminActive(new ComponentName(context, MyDeviceAdminReceiver.class)));
        
        if ("com.securelock.LOCK_DEVICE".equals(action)) {
            ComponentName compName = new ComponentName(context, MyDeviceAdminReceiver.class);
            Log.d(TAG, "Checking device admin status...");
            
            if (!dpm.isAdminActive(compName)) {
                Log.e(TAG, "Device admin not active!");
                Toast.makeText(context, "Device admin active nahi hai! Settings mein enable karein.", Toast.LENGTH_LONG).show();
                return;
            }
            
            Log.d(TAG, "Device admin active, processing lock request...");
            
            // Handle test cases
            if ("TEST".equals(day)) {
                Log.d(TAG, "Test lock triggered");
                
                // Use default 5-minute lock for testing
                int durationMinutes = 5;
                String label = "Test Lock";
                
                // Start the lock service
                Intent serviceIntent = new Intent(context, LockService.class);
                serviceIntent.putExtra("duration", durationMinutes);
                serviceIntent.putExtra("schedule_label", label);
                
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(serviceIntent);
                } else {
                    context.startService(serviceIntent);
                }
                
                Log.d(TAG, "Test lock service started for " + durationMinutes + " minutes!");
                Toast.makeText(context, "Test Lock activated! Device locked for " + durationMinutes + " minutes!", Toast.LENGTH_LONG).show();
                return;
            }
            
            // Find the schedule that matches this day and current time
            int scheduleCount = prefs.getInt("schedule_count", 0);
            Log.d(TAG, "Total schedules found: " + scheduleCount);
            
            // Get current time for validation
            Calendar now = Calendar.getInstance();
            int currentHour = now.get(Calendar.HOUR_OF_DAY);
            int currentMinute = now.get(Calendar.MINUTE);
            
            // Try to find matching schedule first
            boolean scheduleFound = false;
            for (int i = 0; i < scheduleCount; i++) {
                String prefix = "schedule_" + i + "_";
                Set<String> scheduleDays = prefs.getStringSet(prefix + "days", null);
                
                if (scheduleDays != null && scheduleDays.contains(day)) {
                    // This schedule matches the day, get its times
                    int startHour = prefs.getInt(prefix + "start_hour", 9);
                    int startMinute = prefs.getInt(prefix + "start_minute", 0);
                    int endHour = prefs.getInt(prefix + "end_hour", 17);
                    int endMinute = prefs.getInt(prefix + "end_minute", 0);
                    String label = prefs.getString(prefix + "label", "Scheduled Lock");
                    
                    Log.d(TAG, "Found matching schedule " + i + ": " + label);
                    Log.d(TAG, "Schedule times: " + startHour + ":" + String.format("%02d", startMinute) + 
                          " to " + endHour + ":" + String.format("%02d", endMinute));
                    Log.d(TAG, "Current time: " + currentHour + ":" + String.format("%02d", currentMinute));
                    
                    // Validate that this is the correct time to trigger
                    if (hour == startHour && minute == startMinute) {
                        // Calculate duration in minutes
                        int startTotalMinutes = startHour * 60 + startMinute;
                        int endTotalMinutes = endHour * 60 + endMinute;
                        int durationMinutes = endTotalMinutes - startTotalMinutes;
                        
                        if (durationMinutes <= 0) {
                            durationMinutes += 24 * 60; // Add 24 hours if end time is next day
                        }
                        
                        Log.d(TAG, "Activating schedule: " + label + " for " + durationMinutes + " minutes");
                        
                        // Start the lock service
                        Intent serviceIntent = new Intent(context, LockService.class);
                        serviceIntent.putExtra("duration", durationMinutes);
                        serviceIntent.putExtra("schedule_label", label);
                        
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            context.startForegroundService(serviceIntent);
                        } else {
                            context.startService(serviceIntent);
                        }
                        
                        Log.d(TAG, "Lock service started for " + durationMinutes + " minutes!");
                        Toast.makeText(context, "Schedule '" + label + "' activated! Device locked for " + 
                                     formatDuration(durationMinutes) + "!", Toast.LENGTH_LONG).show();
                        scheduleFound = true;
                        break;
                    } else {
                        Log.d(TAG, "Time mismatch - expected: " + hour + ":" + String.format("%02d", minute) + 
                              ", schedule start: " + startHour + ":" + String.format("%02d", startMinute));
                    }
                }
            }
            
            // If no specific schedule found, check if this is a valid alarm trigger
            if (!scheduleFound) {
                Log.w(TAG, "No matching schedule found for day: " + day + " at time: " + hour + ":" + String.format("%02d", minute));
                Toast.makeText(context, "Schedule not found! Please check your settings.", Toast.LENGTH_LONG).show();
            }
            
        } else if ("com.securelock.UNLOCK_DEVICE".equals(action)) {
            Log.d(TAG, "Unlock time reached for day: " + day + " at " + hour + ":" + String.format("%02d", minute));
            
            // Stop any running lock service
            Intent stopServiceIntent = new Intent(context, LockService.class);
            context.stopService(stopServiceIntent);
            
            Toast.makeText(context, "Schedule complete! Device unlocked.", Toast.LENGTH_SHORT).show();
        }
    }
    
    private String formatDuration(int minutes) {
        if (minutes < 60) {
            return minutes + " minutes";
        } else {
            int hours = minutes / 60;
            int remainingMinutes = minutes % 60;
            if (remainingMinutes == 0) {
                return hours + " hour" + (hours > 1 ? "s" : "");
            } else {
                return hours + " hour" + (hours > 1 ? "s" : "") + " " + remainingMinutes + " minutes";
            }
        }
    }
} 