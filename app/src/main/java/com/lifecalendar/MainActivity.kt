package com.lifecalendar

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.lifecalendar.databinding.ActivityMainBinding
import java.time.LocalDate
import java.time.Year
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefsManager: PreferencesManager
    private lateinit var wallpaperGenerator: WallpaperGenerator
    private lateinit var yearTrackerGenerator: YearTrackerGenerator
    
    private var selectedDate: LocalDate? = null
    private var isYearTrackerMode = false
    
    // Flag to prevent listener from firing during programmatic switch changes
    private var isRestoringState = false

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("life_calendar_prefs", Context.MODE_PRIVATE)
        val language = prefs.getString("language", "en") ?: "en"
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        prefsManager = PreferencesManager(this)
        
        // Check first launch — redirect to language selection
        if (prefsManager.isFirstLaunch()) {
            val intent = Intent(this, LanguageSelectionActivity::class.java)
            startActivity(intent)
            finish()
            return
        }
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        wallpaperGenerator = WallpaperGenerator(this)
        yearTrackerGenerator = YearTrackerGenerator(this)

        setupUI()
        loadSavedData()
        updateYearStats()
    }

    private fun setupUI() {
        // Wallpaper type toggle
        binding.toggleWallpaperType.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                isYearTrackerMode = (checkedId == R.id.btnYearTracker)
                updateUIForMode()
                
                // Save preference immediately when toggling
                prefsManager.saveWallpaperType(
                    if (isYearTrackerMode) PreferencesManager.TYPE_YEAR_TRACKER 
                    else PreferencesManager.TYPE_LIFE_CALENDAR
                )
            }
        }
        
        // Days position switch
        binding.switchDaysPosition.setOnCheckedChangeListener { _, isChecked ->
            binding.tvPositionHint.text = if (isChecked) getString(R.string.position_top) else getString(R.string.position_bottom)
            prefsManager.saveDaysPositionTop(isChecked)
        }
        
        // Default to Life Calendar mode
        binding.btnLifeCalendar.isChecked = true

        // Date picker button
        binding.btnSelectDate.setOnClickListener {
            showDatePicker()
        }

        // Life expectancy slider
        binding.sliderLifeExpectancy.addOnChangeListener { _, value, _ ->
            val years = value.toInt()
            binding.tvLifeExpectancy.text = getString(R.string.years_format, years)
            updateStats()
        }

        // Set wallpaper now button
        binding.btnSetWallpaper.setOnClickListener {
            if (isYearTrackerMode) {
                setYearTrackerWallpaper()
            } else {
                setLifeCalendarWallpaper()
            }
        }

        // Enable/disable auto-update switch
        binding.switchAutoUpdate.setOnCheckedChangeListener { _, isChecked ->
            if (isRestoringState) return@setOnCheckedChangeListener
            
            prefsManager.saveAutoUpdateEnabled(isChecked)
            
            if (isChecked) {
                scheduleMidnightUpdate()
                Toast.makeText(this, getString(R.string.auto_update_enabled), Toast.LENGTH_SHORT).show()
            } else {
                cancelMidnightUpdate()
                Toast.makeText(this, getString(R.string.auto_update_disabled), Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun updateUIForMode() {
        if (isYearTrackerMode) {
            // Show Year Tracker UI, hide Life Calendar specific UI
            binding.cardBirthdate.visibility = View.GONE
            binding.cardLifeExpectancy.visibility = View.GONE
            binding.cardLifeStats.visibility = View.GONE
            binding.cardYearStats.visibility = View.VISIBLE
            binding.cardDaysPosition.visibility = View.VISIBLE
            updateYearStats()
        } else {
            // Show Life Calendar UI
            binding.cardBirthdate.visibility = View.VISIBLE
            binding.cardLifeExpectancy.visibility = View.VISIBLE
            binding.cardLifeStats.visibility = View.VISIBLE
            binding.cardYearStats.visibility = View.GONE
            binding.cardDaysPosition.visibility = View.GONE
            updateStats()
        }
    }

    private fun loadSavedData() {
        isRestoringState = true
        
        // Load birthdate
        prefsManager.getBirthdate()?.let { date ->
            selectedDate = date
            binding.tvSelectedDate.text = formatDate(date)
            updateStats()
        }

        // Load life expectancy
        val lifeExpectancy = prefsManager.getLifeExpectancy()
        binding.sliderLifeExpectancy.value = lifeExpectancy.toFloat()
        binding.tvLifeExpectancy.text = getString(R.string.years_format, lifeExpectancy)
        
        // Load days position preference
        val isDaysTop = prefsManager.isDaysPositionTop()
        binding.switchDaysPosition.isChecked = isDaysTop
        binding.tvPositionHint.text = if (isDaysTop) getString(R.string.position_top) else getString(R.string.position_bottom)
        
        // Load wallpaper type
        val wallpaperType = prefsManager.getWallpaperType()
        if (wallpaperType == PreferencesManager.TYPE_YEAR_TRACKER) {
            binding.btnYearTracker.isChecked = true
        } else {
            binding.btnLifeCalendar.isChecked = true
        }
        
        // Load auto-update state
        val autoUpdateEnabled = prefsManager.isAutoUpdateEnabled()
        binding.switchAutoUpdate.isChecked = autoUpdateEnabled
        
        // Re-schedule alarm if it was previously enabled
        if (autoUpdateEnabled) {
            scheduleMidnightUpdate()
        }
        
        isRestoringState = false
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        
        // If date was previously selected, use that
        selectedDate?.let {
            calendar.set(it.year, it.monthValue - 1, it.dayOfMonth)
        }

        val datePicker = DatePickerDialog(
            this,
            R.style.DatePickerTheme,
            { _, year, month, dayOfMonth ->
                selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                selectedDate?.let {
                    binding.tvSelectedDate.text = formatDate(it)
                    prefsManager.saveBirthdate(it)
                    updateStats()
                }
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Limit date selection to past dates
        datePicker.datePicker.maxDate = System.currentTimeMillis()
        datePicker.show()
    }

    private fun formatDate(date: LocalDate): String {
        val months = arrayOf(
            getString(R.string.month_january), getString(R.string.month_february),
            getString(R.string.month_march), getString(R.string.month_april),
            getString(R.string.month_may), getString(R.string.month_june),
            getString(R.string.month_july), getString(R.string.month_august),
            getString(R.string.month_september), getString(R.string.month_october),
            getString(R.string.month_november), getString(R.string.month_december)
        )
        return getString(R.string.date_format, months[date.monthValue - 1], date.dayOfMonth, date.year)
    }

    private fun updateStats() {
        if (selectedDate == null) return

        val weeksLived = prefsManager.calculateWeeksLived()
        val lifeExpectancy = binding.sliderLifeExpectancy.value.toInt()
        val totalWeeks = lifeExpectancy * 52
        val weeksRemaining = (totalWeeks - weeksLived).coerceAtLeast(0)
        val percentLived = ((weeksLived.toFloat() / totalWeeks) * 100).coerceIn(0f, 100f)

        binding.tvWeeksLived.text = "%,d".format(weeksLived)
        binding.tvWeeksRemaining.text = "%,d".format(weeksRemaining)
        binding.tvPercentLived.text = "%.1f%%".format(percentLived)
        binding.progressLife.progress = percentLived.toInt()
    }
    
    private fun updateYearStats() {
        val today = LocalDate.now()
        val dayOfYear = today.dayOfYear
        val year = today.year
        val totalDays = if (Year.of(year).isLeap) 366 else 365
        val daysRemaining = (totalDays - dayOfYear).coerceAtLeast(0)
        val percentProgress = ((dayOfYear.toFloat() / totalDays) * 100).coerceIn(0f, 100f)
        
        binding.tvDaysPassed.text = "%,d".format(dayOfYear)
        binding.tvDaysRemaining.text = "%,d".format(daysRemaining)
        binding.tvYearProgress.text = "%.1f%%".format(percentProgress)
        binding.progressYear.progress = percentProgress.toInt()
    }

    private fun setLifeCalendarWallpaper() {
        if (selectedDate == null) {
            Toast.makeText(this, getString(R.string.select_birthdate_first), Toast.LENGTH_SHORT).show()
            return
        }

        // Save life expectancy
        val lifeExpectancy = binding.sliderLifeExpectancy.value.toInt()
        prefsManager.saveLifeExpectancy(lifeExpectancy)

        // Generate and set wallpaper
        val weeksLived = prefsManager.calculateWeeksLived()
        
        try {
            val bitmap = wallpaperGenerator.generateWallpaper(weeksLived, lifeExpectancy)
            wallpaperGenerator.setWallpaper(bitmap)
            Toast.makeText(this, getString(R.string.wallpaper_set_life), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.wallpaper_failed, e.message), Toast.LENGTH_LONG).show()
        }
        
        // Save wallpaper type for auto-updates
        prefsManager.saveWallpaperType(PreferencesManager.TYPE_LIFE_CALENDAR)
    }
    
    private fun setYearTrackerWallpaper() {
        val today = LocalDate.now()
        val dayOfYear = today.dayOfYear
        val year = today.year
        
        // Get position preference
        val isDaysTop = prefsManager.isDaysPositionTop()
        
        try {
            val bitmap = yearTrackerGenerator.generateWallpaper(dayOfYear, year, isDaysTop)
            yearTrackerGenerator.setWallpaper(bitmap)
            Toast.makeText(this, getString(R.string.wallpaper_set_year), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.wallpaper_failed, e.message), Toast.LENGTH_LONG).show()
        }
        
        // Save wallpaper type for auto-updates
        prefsManager.saveWallpaperType(PreferencesManager.TYPE_YEAR_TRACKER)
    }

    private fun scheduleMidnightUpdate() {
        MidnightWallpaperReceiver.scheduleMidnightAlarm(this)
    }

    private fun cancelMidnightUpdate() {
        MidnightWallpaperReceiver.cancelMidnightAlarm(this)
    }
}
