package com.lifecalendar

import android.content.Context
import android.content.SharedPreferences
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Manages saving/loading user preferences (birthdate, life expectancy)
 */
class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "life_calendar_prefs"
        private const val KEY_BIRTHDATE = "birthdate"
        private const val KEY_LIFE_EXPECTANCY = "life_expectancy"
        private const val KEY_WALLPAPER_TYPE = "wallpaper_type"
        private const val KEY_DAYS_POSITION_TOP = "days_position_top"
        private const val KEY_AUTO_UPDATE_ENABLED = "auto_update_enabled"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val DEFAULT_LIFE_EXPECTANCY = 80
        
        const val TYPE_LIFE_CALENDAR = 0
        const val TYPE_YEAR_TRACKER = 1
    }

    /**
     * Save birthdate as ISO string (YYYY-MM-DD)
     */
    fun saveBirthdate(date: LocalDate) {
        prefs.edit().putString(KEY_BIRTHDATE, date.format(DateTimeFormatter.ISO_LOCAL_DATE)).apply()
    }

    /**
     * Get saved birthdate, or null if not set
     */
    fun getBirthdate(): LocalDate? {
        val dateStr = prefs.getString(KEY_BIRTHDATE, null) ?: return null
        return try {
            LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Save life expectancy in years
     */
    fun saveLifeExpectancy(years: Int) {
        prefs.edit().putInt(KEY_LIFE_EXPECTANCY, years).apply()
    }

    /**
     * Get life expectancy (default: 80 years)
     */
    fun getLifeExpectancy(): Int {
        return prefs.getInt(KEY_LIFE_EXPECTANCY, DEFAULT_LIFE_EXPECTANCY)
    }

    /**
     * Calculate weeks lived from birthdate to today
     */
    fun calculateWeeksLived(): Int {
        val birthdate = getBirthdate() ?: return 0
        val today = LocalDate.now()
        val daysBetween = ChronoUnit.DAYS.between(birthdate, today)
        return (daysBetween / 7).toInt().coerceAtLeast(0)
    }

    /**
     * Check if birthdate has been set
     */
    fun isBirthdateSet(): Boolean {
        return getBirthdate() != null
    }
    
    /**
     * Save wallpaper type (Life Calendar or Year Tracker)
     */
    fun saveWallpaperType(type: Int) {
        prefs.edit().putInt(KEY_WALLPAPER_TYPE, type).apply()
    }
    
    /**
     * Get wallpaper type (default: Life Calendar)
     */
    fun getWallpaperType(): Int {
        return prefs.getInt(KEY_WALLPAPER_TYPE, TYPE_LIFE_CALENDAR)
    }
    
    /**
     * Save days counter position preference for Year Tracker
     * true = top, false = bottom
     */
    fun saveDaysPositionTop(isTop: Boolean) {
        prefs.edit().putBoolean(KEY_DAYS_POSITION_TOP, isTop).apply()
    }
    
    /**
     * Get days counter position (default: false/bottom)
     */
    fun isDaysPositionTop(): Boolean {
        return prefs.getBoolean(KEY_DAYS_POSITION_TOP, false)
    }
    
    /**
     * Save auto-update enabled state
     */
    fun saveAutoUpdateEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_UPDATE_ENABLED, enabled).apply()
    }
    
    /**
     * Get auto-update enabled state (default: false)
     */
    fun isAutoUpdateEnabled(): Boolean {
        return prefs.getBoolean(KEY_AUTO_UPDATE_ENABLED, false)
    }
    
    /**
     * Save selected language ("en" or "es")
     */
    fun saveLanguage(language: String) {
        prefs.edit().putString(KEY_LANGUAGE, language).apply()
    }
    
    /**
     * Get saved language (default: "en")
     */
    fun getLanguage(): String {
        return prefs.getString(KEY_LANGUAGE, "en") ?: "en"
    }
    
    /**
     * Check if this is the first launch
     */
    fun isFirstLaunch(): Boolean {
        return prefs.getBoolean(KEY_FIRST_LAUNCH, true)
    }
    
    /**
     * Mark first launch as complete
     */
    fun setFirstLaunchComplete() {
        prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
    }
}

