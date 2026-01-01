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
        private const val DEFAULT_LIFE_EXPECTANCY = 80
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
}
