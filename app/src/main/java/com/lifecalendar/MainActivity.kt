package com.lifecalendar

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.lifecalendar.databinding.ActivityMainBinding
import java.time.LocalDate
import java.time.Year
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefsManager: PreferencesManager
    private lateinit var wallpaperGenerator: WallpaperGenerator
    private lateinit var yearTrackerGenerator: YearTrackerGenerator
    
    private var selectedDate: LocalDate? = null
    private var isYearTrackerMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefsManager = PreferencesManager(this)
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
            binding.tvPositionHint.text = if (isChecked) "Currently at top" else "Currently at bottom"
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
            binding.tvLifeExpectancy.text = "$years years"
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
            if (isChecked) {
                scheduleMidnightUpdate()
                Toast.makeText(this, "Midnight auto-updates enabled", Toast.LENGTH_SHORT).show()
            } else {
                cancelMidnightUpdate()
                Toast.makeText(this, "Midnight updates disabled", Toast.LENGTH_SHORT).show()
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
        // Load birthdate
        prefsManager.getBirthdate()?.let { date ->
            selectedDate = date
            binding.tvSelectedDate.text = formatDate(date)
            updateStats()
        }

        // Load life expectancy
        val lifeExpectancy = prefsManager.getLifeExpectancy()
        binding.sliderLifeExpectancy.value = lifeExpectancy.toFloat()
        binding.tvLifeExpectancy.text = "$lifeExpectancy years"
        
        // Load days position preference
        val isDaysTop = prefsManager.isDaysPositionTop()
        binding.switchDaysPosition.isChecked = isDaysTop
        binding.tvPositionHint.text = if (isDaysTop) "Currently at top" else "Currently at bottom"
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
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        return "${months[date.monthValue - 1]} ${date.dayOfMonth}, ${date.year}"
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
            Toast.makeText(this, "Please select your birthdate first", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(this, "Life Calendar wallpaper set! 🎉", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to set wallpaper: ${e.message}", Toast.LENGTH_LONG).show()
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
            Toast.makeText(this, "Year Tracker wallpaper set! 🎉", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to set wallpaper: ${e.message}", Toast.LENGTH_LONG).show()
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
