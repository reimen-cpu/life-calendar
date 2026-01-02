package com.lifecalendar

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.time.LocalDate
import java.time.Year
import java.util.Calendar

/**
 * BroadcastReceiver that updates wallpaper at midnight
 */
class MidnightWallpaperReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        try {
            val prefsManager = PreferencesManager(context)
            val wallpaperType = prefsManager.getWallpaperType()
            
            if (wallpaperType == PreferencesManager.TYPE_YEAR_TRACKER) {
                // Update Year Tracker wallpaper
                val yearTrackerGenerator = YearTrackerGenerator(context)
                val today = LocalDate.now()
                val isDaysTop = prefsManager.isDaysPositionTop()
                val bitmap = yearTrackerGenerator.generateWallpaper(today.dayOfYear, today.year, isDaysTop)
                yearTrackerGenerator.setWallpaper(bitmap)
            } else {
                // Update Life Calendar wallpaper
                val wallpaperGenerator = WallpaperGenerator(context)
                val weeksLived = prefsManager.calculateWeeksLived()
                val lifeExpectancy = prefsManager.getLifeExpectancy()
                
                if (weeksLived >= 0) {
                    val bitmap = wallpaperGenerator.generateWallpaper(weeksLived, lifeExpectancy)
                    wallpaperGenerator.setWallpaper(bitmap)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        // Schedule next midnight alarm
        scheduleMidnightAlarm(context)
    }

    companion object {
        private const val REQUEST_CODE = 1001
        
        /**
         * Schedule wallpaper update for next midnight (12:00 AM)
         */
        fun scheduleMidnightAlarm(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            
            val intent = Intent(context, MidnightWallpaperReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            // Calculate next midnight
            val calendar = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 5)  // 5 seconds after midnight to be safe
                set(Calendar.MILLISECOND, 0)
            }
            
            // Use setExactAndAllowWhileIdle for reliable midnight updates
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
        
        /**
         * Cancel the midnight alarm
         */
        fun cancelMidnightAlarm(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            
            val intent = Intent(context, MidnightWallpaperReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            alarmManager.cancel(pendingIntent)
        }
    }
}
