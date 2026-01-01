package com.lifecalendar

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import com.lifecalendar.databinding.ActivityMainBinding
import java.time.LocalDate
import java.util.Calendar
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var prefsManager: PreferencesManager
    private lateinit var wallpaperGenerator: WallpaperGenerator
    
    private var selectedDate: LocalDate? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefsManager = PreferencesManager(this)
        wallpaperGenerator = WallpaperGenerator(this)

        setupUI()
        loadSavedData()
    }

    private fun setupUI() {
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
            setWallpaperNow()
        }

        // Enable/disable auto-update switch
        binding.switchAutoUpdate.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                scheduleWeeklyUpdate()
                Toast.makeText(this, "Weekly updates enabled", Toast.LENGTH_SHORT).show()
            } else {
                cancelWeeklyUpdate()
                Toast.makeText(this, "Weekly updates disabled", Toast.LENGTH_SHORT).show()
            }
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

    private fun setWallpaperNow() {
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
            Toast.makeText(this, "Wallpaper set successfully! 🎉", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to set wallpaper: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun scheduleWeeklyUpdate() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        // Run every 7 days
        val workRequest = PeriodicWorkRequestBuilder<WallpaperWorker>(7, TimeUnit.DAYS)
            .setConstraints(constraints)
            .setInitialDelay(calculateDelayToNextSunday(), TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            WallpaperWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    private fun cancelWeeklyUpdate() {
        WorkManager.getInstance(this).cancelUniqueWork(WallpaperWorker.WORK_NAME)
    }

    private fun calculateDelayToNextSunday(): Long {
        val now = Calendar.getInstance()
        val daysUntilSunday = (Calendar.SUNDAY - now.get(Calendar.DAY_OF_WEEK) + 7) % 7
        
        // If today is Sunday, schedule for next Sunday
        val actualDays = if (daysUntilSunday == 0) 7 else daysUntilSunday
        
        return TimeUnit.DAYS.toMillis(actualDays.toLong())
    }
}
