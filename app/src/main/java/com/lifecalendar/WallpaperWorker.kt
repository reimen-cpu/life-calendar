package com.lifecalendar

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

/**
 * WorkManager Worker that runs weekly to update the wallpaper
 */
class WallpaperWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    companion object {
        const val TAG = "WallpaperWorker"
        const val WORK_NAME = "life_calendar_weekly_update"
    }

    override fun doWork(): Result {
        Log.d(TAG, "Starting weekly wallpaper update...")

        return try {
            val prefsManager = PreferencesManager(applicationContext)
            
            // Check if birthdate is set
            if (!prefsManager.isBirthdateSet()) {
                Log.w(TAG, "Birthdate not set, skipping wallpaper update")
                return Result.success()
            }

            // Calculate weeks lived
            val weeksLived = prefsManager.calculateWeeksLived()
            val lifeExpectancy = prefsManager.getLifeExpectancy()

            Log.d(TAG, "Generating wallpaper: $weeksLived weeks lived, $lifeExpectancy year expectancy")

            // Generate and set wallpaper
            val generator = WallpaperGenerator(applicationContext)
            val bitmap = generator.generateWallpaper(weeksLived, lifeExpectancy)
            generator.setWallpaper(bitmap)

            Log.d(TAG, "Wallpaper updated successfully!")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update wallpaper", e)
            Result.retry()
        }
    }
}
