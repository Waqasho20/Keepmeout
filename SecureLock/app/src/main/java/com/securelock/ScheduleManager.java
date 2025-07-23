package com.securelock;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import java.util.Calendar;
import java.util.Set;
import android.content.ComponentName;

public class ScheduleManager {
    private static final String TAG = "ScheduleManager";
    private static final String PREFS_NAME = "SecureLockPrefs";
    private static final String SCHEDULE_SET_TIME = "schedule_set_time";
    private static final long RESTRICT_DAYS = 15 * 24 * 60 * 60 * 1000L;

    public static boolean canEditSchedule(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long lastSet = prefs.getLong(SCHEDULE_SET_TIME, 0);
        long now = System.currentTimeMillis();
        return (now - lastSet) >= RESTRICT_DAYS;
    }

    public static void setLockSchedule(Context context, int startHour, int startMinute, 
                                     int endHour, int endMinute, Set<String> selectedDays) {
        Log.d(TAG, "Setting lock schedule: " + startHour + ":" + startMinute + " to " + endHour + ":" + endMinute);
        Log.d(TAG, "Selected days: " + selectedDays);
        
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        // Cancel existing alarms first
        cancelExistingAlarms(context);
        
        // Set new alarms for each selected day
        for (String day : selectedDays) {
            int dayOfWeek = getDayOfWeek(day);
            Log.d(TAG, "Setting alarms for day: " + day + " (dayOfWeek: " + dayOfWeek + ")");
            
            // Set lock alarm for this day
            setAlarmForDay(context, alarmManager, day, startHour, startMinute, true);
            
            // Set unlock alarm for this day
            setAlarmForDay(context, alarmManager, day, endHour, endMinute, false);
        }

        // Save schedule timestamp
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(SCHEDULE_SET_TIME, System.currentTimeMillis());
        editor.apply();
        
        Log.d(TAG, "Schedule saved successfully.");
    }
    
    private static void setAlarmForDay(Context context, AlarmManager alarmManager, 
                                     String day, int hour, int minute, boolean isLock) {
        Calendar alarmTime = Calendar.getInstance();
        alarmTime.set(Calendar.DAY_OF_WEEK, getDayOfWeek(day));
        alarmTime.set(Calendar.HOUR_OF_DAY, hour);
        alarmTime.set(Calendar.MINUTE, minute);
        alarmTime.set(Calendar.SECOND, 0);
        alarmTime.set(Calendar.MILLISECOND, 0);
        
        // Current time for comparison
        Calendar now = Calendar.getInstance();
        
        // If alarm time has passed today, schedule for next week
        if (alarmTime.getTimeInMillis() <= now.getTimeInMillis()) {
            alarmTime.add(Calendar.WEEK_OF_YEAR, 1);
            Log.d(TAG, "Time has passed, scheduling for next week");
        }
        
        // Additional check: if it's the same day and time has passed, schedule for next week
        if (alarmTime.get(Calendar.DAY_OF_WEEK) == now.get(Calendar.DAY_OF_WEEK)) {
            int currentTotalMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
            int alarmTotalMinutes = hour * 60 + minute;
            
            if (alarmTotalMinutes <= currentTotalMinutes) {
                alarmTime.add(Calendar.WEEK_OF_YEAR, 1);
                Log.d(TAG, "Same day but time passed, scheduling for next week");
            }
        }
        
        String action = isLock ? "com.securelock.LOCK_DEVICE" : "com.securelock.UNLOCK_DEVICE";
        Intent intent = new Intent(action);
        intent.putExtra("day", day);
        intent.putExtra("hour", hour);
        intent.putExtra("minute", minute);
        // Explicitly set the receiver component
        intent.setComponent(new ComponentName(context, LockReceiver.class));
        
        int requestCode = getRequestCode(day, isLock);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        // Use setExactAndAllowWhileIdle for more reliable alarms
        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), pendingIntent);
            
            String type = isLock ? "Lock" : "Unlock";
            Log.d(TAG, type + " alarm set for " + day + " at " + hour + ":" + String.format("%02d", minute) + 
                  " (trigger time: " + alarmTime.getTime() + ")");
        } catch (SecurityException e) {
            Log.e(TAG, "Permission denied for exact alarm: " + e.getMessage());
            // Fallback to regular alarm
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), pendingIntent);
        }
    }
    
    // Test method to manually trigger lock for testing
    public static void testLockNow(Context context) {
        Log.d(TAG, "Testing lock now...");
        Intent lockIntent = new Intent("com.securelock.LOCK_DEVICE");
        lockIntent.putExtra("day", "TEST");
        lockIntent.putExtra("hour", Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
        lockIntent.putExtra("minute", Calendar.getInstance().get(Calendar.MINUTE));
        // Explicitly set the receiver component
        lockIntent.setComponent(new ComponentName(context, LockReceiver.class));
        context.sendBroadcast(lockIntent);
    }
    
    // Method to set alarm for next minute for testing
    public static void setTestAlarm(Context context) {
        Log.d(TAG, "Setting test alarm for next minute...");
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        Calendar testTime = Calendar.getInstance();
        testTime.add(Calendar.MINUTE, 1); // 1 minute from now
        
        Intent lockIntent = new Intent("com.securelock.LOCK_DEVICE");
        lockIntent.putExtra("day", "TEST");
        lockIntent.putExtra("hour", testTime.get(Calendar.HOUR_OF_DAY));
        lockIntent.putExtra("minute", testTime.get(Calendar.MINUTE));
        // Explicitly set the receiver component
        lockIntent.setComponent(new ComponentName(context, LockReceiver.class));
        
        PendingIntent lockPending = PendingIntent.getBroadcast(context, 999, lockIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, testTime.getTimeInMillis(), lockPending);
            Log.d(TAG, "Test alarm set for: " + testTime.getTime());
        } catch (SecurityException e) {
            Log.e(TAG, "Permission denied for test alarm: " + e.getMessage());
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, testTime.getTimeInMillis(), lockPending);
        }
    }
    
    // Method to set alarm for next 30 seconds for immediate testing
    public static void setImmediateTestAlarm(Context context) {
        Log.d(TAG, "Setting immediate test alarm for next 30 seconds...");
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        Calendar testTime = Calendar.getInstance();
        testTime.add(Calendar.SECOND, 30); // 30 seconds from now
        
        Intent lockIntent = new Intent("com.securelock.LOCK_DEVICE");
        lockIntent.putExtra("day", "TEST");
        lockIntent.putExtra("hour", testTime.get(Calendar.HOUR_OF_DAY));
        lockIntent.putExtra("minute", testTime.get(Calendar.MINUTE));
        // Explicitly set the receiver component
        lockIntent.setComponent(new ComponentName(context, LockReceiver.class));
        
        PendingIntent lockPending = PendingIntent.getBroadcast(context, 998, lockIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        
        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, testTime.getTimeInMillis(), lockPending);
            Log.d(TAG, "Immediate test alarm set for: " + testTime.getTime());
        } catch (SecurityException e) {
            Log.e(TAG, "Permission denied for immediate test alarm: " + e.getMessage());
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, testTime.getTimeInMillis(), lockPending);
        }
    }
    
    // Method to check if current time matches any schedule
    public static boolean isScheduleActiveNow(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int scheduleCount = prefs.getInt("schedule_count", 0);
        
        Calendar now = Calendar.getInstance();
        int currentDayOfWeek = now.get(Calendar.DAY_OF_WEEK);
        int currentHour = now.get(Calendar.HOUR_OF_DAY);
        int currentMinute = now.get(Calendar.MINUTE);
        int currentTotalMinutes = currentHour * 60 + currentMinute;
        
        String currentDay = getDayString(currentDayOfWeek);
        
        for (int i = 0; i < scheduleCount; i++) {
            String prefix = "schedule_" + i + "_";
            Set<String> scheduleDays = prefs.getStringSet(prefix + "days", null);
            
            if (scheduleDays != null && scheduleDays.contains(currentDay)) {
                int startHour = prefs.getInt(prefix + "start_hour", 9);
                int startMinute = prefs.getInt(prefix + "start_minute", 0);
                int endHour = prefs.getInt(prefix + "end_hour", 17);
                int endMinute = prefs.getInt(prefix + "end_minute", 0);
                
                int startTotalMinutes = startHour * 60 + startMinute;
                int endTotalMinutes = endHour * 60 + endMinute;
                
                // Handle overnight schedules
                if (endTotalMinutes <= startTotalMinutes) {
                    endTotalMinutes += 24 * 60; // Add 24 hours
                    if (currentTotalMinutes < startTotalMinutes) {
                        currentTotalMinutes += 24 * 60; // Add 24 hours to current time too
                    }
                }
                
                if (currentTotalMinutes >= startTotalMinutes && currentTotalMinutes < endTotalMinutes) {
                    Log.d(TAG, "Schedule " + i + " is active now");
                    return true;
                }
            }
        }
        
        return false;
    }
    
    private static String getDayString(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.SUNDAY: return "S";
            case Calendar.MONDAY: return "M";
            case Calendar.TUESDAY: return "T";
            case Calendar.WEDNESDAY: return "W";
            case Calendar.THURSDAY: return "R";
            case Calendar.FRIDAY: return "F";
            case Calendar.SATURDAY: return "A";
            default: return "M";
        }
    }
    
    private static void cancelExistingAlarms(Context context) {
        Log.d(TAG, "Cancelling existing alarms");
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        // Cancel all possible alarms (for all days)
        String[] days = {"S", "M", "T", "W", "R", "F", "A"};
        for (String day : days) {
            // Cancel lock alarms
            Intent lockIntent = new Intent("com.securelock.LOCK_DEVICE");
            lockIntent.setComponent(new ComponentName(context, LockReceiver.class));
            PendingIntent lockPending = PendingIntent.getBroadcast(context, 
                getRequestCode(day, true), lockIntent, 
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
            if (lockPending != null) {
                alarmManager.cancel(lockPending);
                lockPending.cancel();
                Log.d(TAG, "Cancelled lock alarm for " + day);
            }
            
            // Cancel unlock alarms
            Intent unlockIntent = new Intent("com.securelock.UNLOCK_DEVICE");
            unlockIntent.setComponent(new ComponentName(context, LockReceiver.class));
            PendingIntent unlockPending = PendingIntent.getBroadcast(context, 
                getRequestCode(day, false), unlockIntent, 
                PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
            if (unlockPending != null) {
                alarmManager.cancel(unlockPending);
                unlockPending.cancel();
                Log.d(TAG, "Cancelled unlock alarm for " + day);
            }
        }
        
        // Cancel test alarms
        Intent testIntent = new Intent("com.securelock.LOCK_DEVICE");
        testIntent.setComponent(new ComponentName(context, LockReceiver.class));
        PendingIntent testPending = PendingIntent.getBroadcast(context, 999, testIntent, 
            PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        if (testPending != null) {
            alarmManager.cancel(testPending);
            testPending.cancel();
            Log.d(TAG, "Cancelled test alarm");
        }
        
        PendingIntent immediateTestPending = PendingIntent.getBroadcast(context, 998, testIntent, 
            PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        if (immediateTestPending != null) {
            alarmManager.cancel(immediateTestPending);
            immediateTestPending.cancel();
            Log.d(TAG, "Cancelled immediate test alarm");
        }
    }
    
    private static int getDayOfWeek(String day) {
        switch (day) {
            case "S": return Calendar.SUNDAY;
            case "M": return Calendar.MONDAY;
            case "T": return Calendar.TUESDAY;
            case "W": return Calendar.WEDNESDAY;
            case "R": return Calendar.THURSDAY;
            case "F": return Calendar.FRIDAY;
            case "A": return Calendar.SATURDAY;
            default: return Calendar.MONDAY;
        }
    }
    
    private static int getRequestCode(String day, boolean isLock) {
        int dayCode = 0;
        switch (day) {
            case "S": dayCode = 1; break;
            case "M": dayCode = 2; break;
            case "T": dayCode = 3; break;
            case "W": dayCode = 4; break;
            case "R": dayCode = 5; break;
            case "F": dayCode = 6; break;
            case "A": dayCode = 7; break;
        }
        return dayCode * 10 + (isLock ? 1 : 2);
    }
} 