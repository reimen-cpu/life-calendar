package com.lifecalendar

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.time.LocalDate
import java.time.Year

/**
 * Background worker to update wallpaper daily
 * Updates both Life Calendar and Year Tracker based on saved preference
 */
class WallpaperWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        return try {
            val prefsManager = PreferencesManager(applicationContext)
            val wallpaperType = prefsManager.getWallpaperType()
            
            if (wallpaperType == PreferencesManager.TYPE_YEAR_TRACKER) {
                // Update Year Tracker wallpaper
                val yearTrackerGenerator = YearTrackerGenerator(applicationContext)
                val today = LocalDate.now()
                val isDaysTop = prefsManager.isDaysPositionTop()
                val bitmap = yearTrackerGenerator.generateWallpaper(today.dayOfYear, today.year, isDaysTop)
                yearTrackerGenerator.setWallpaper(bitmap)
            } else {
                // Update Life Calendar wallpaper
                val wallpaperGenerator = WallpaperGenerator(applicationContext)
                val weeksLived = prefsManager.calculateWeeksLived()
                val lifeExpectancy = prefsManager.getLifeExpectancy()
                
                if (weeksLived >= 0) {
                    val bitmap = wallpaperGenerator.generateWallpaper(weeksLived, lifeExpectancy)
                    wallpaperGenerator.setWallpaper(bitmap)
                }
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "wallpaper_daily_update"
    }
}
