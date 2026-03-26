package com.lifecalendar

import android.app.WallpaperManager
import android.content.Context
import android.graphics.*
import android.util.DisplayMetrics
import android.view.WindowManager
import java.time.LocalDate
import java.time.Year
import kotlin.math.min

/**
 * Generates Year Tracker wallpaper bitmap
 * Colored dots for remaining days, black dots for passed days
 * Days left counter at the bottom or top
 */
class YearTrackerGenerator(private val context: Context) {

    private val columns = 15  // Dots per row
    
    // Grayscale colors
    private val grayColor = Color.GRAY
    private val whiteColor = Color.WHITE
    private val redColor = Color.parseColor("#FF4444")


    
    /**
     * Generate wallpaper bitmap for the device screen
     */
    fun generateWallpaper(dayOfYear: Int, year: Int, isDaysTop: Boolean = false): Bitmap {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(metrics)
        
        val width = metrics.widthPixels
        val height = metrics.heightPixels

        return generateBitmap(width, height, dayOfYear, year, isDaysTop)
    }

    /**
     * Generate bitmap with specified dimensions
     */
    fun generateBitmap(
        width: Int,
        height: Int,
        dayOfYear: Int,
        year: Int,
        isDaysTop: Boolean
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Fill background with pure black
        canvas.drawColor(Color.BLACK)

        val totalDays = if (Year.of(year).isLeap) 366 else 365
        val daysRemaining = (totalDays - dayOfYear).coerceAtLeast(0)
        
        // Calculate rows needed
        val rows = (totalDays + columns - 1) / columns
        
        // Increased space for lock screen clock (approx 38% of height)
        val paddingTop = height * 0.38f
        val paddingBottom = height * 0.22f
        val paddingHorizontal = width * 0.08f

        val gridWidth = width - (2 * paddingHorizontal)
        val gridHeight = height - paddingTop - paddingBottom

        // Calculate cell and dot sizes
        val cellWidth = gridWidth / columns
        val cellHeight = gridHeight / rows
        val dotRadius = min(cellWidth, cellHeight) * 0.35f
        val dotSpacing = dotRadius * 0.15f

        // Paint for remaining days (gray dots)
        val remainingPaint = Paint().apply {
            isAntiAlias = true
            color = grayColor
            alpha = 255
            style = Paint.Style.FILL
        }

        // Paint for passed days (white dots)
        val passedPaint = Paint().apply {
            isAntiAlias = true
            color = whiteColor
            alpha = 255
            style = Paint.Style.FILL
        }

        // Paint for current day (red dot)
        val todayPaint = Paint().apply {
            isAntiAlias = true
            color = redColor
            alpha = 255
            style = Paint.Style.FILL
        }

        // Draw grid of dots
        var dayCounter = 0
        for (row in 0 until rows) {
            for (col in 0 until columns) {
                if (dayCounter >= totalDays) break
                
                val x = paddingHorizontal + (col * cellWidth) + (cellWidth / 2)
                val y = paddingTop + (row * cellHeight) + (cellHeight / 2)

                val isToday = dayCounter == dayOfYear - 1
                val isPassed = dayCounter < dayOfYear - 1

                if (isToday) {
                    // Today - show as red
                    canvas.drawCircle(x, y, dotRadius - dotSpacing, todayPaint)
                } else if (isPassed) {
                    // Day has passed - show as white
                    canvas.drawCircle(x, y, dotRadius - dotSpacing, passedPaint)
                } else {
                    // Day remaining - show as gray
                    canvas.drawCircle(x, y, dotRadius - dotSpacing, remainingPaint)
                }

                dayCounter++
            }
        }

        // Progress line (red)
        val barPaint = Paint().apply {
            isAntiAlias = true
            color = redColor
            style = Paint.Style.FILL
        }
        
        val barWidth = width * 0.25f
        val barHeight = 4f
        val progressPercent = ((dayOfYear.toFloat() / totalDays) * 100).coerceAtLeast(1f)
        
        // Progress text (percentage)
        val textPaint = Paint().apply {
            isAntiAlias = true
            color = redColor
            textSize = width * 0.045f
            typeface = Typeface.create("sans-serif-bold", Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        
        val progressY = height - height * 0.08f
        canvas.drawText("${progressPercent.toInt()}%", width / 2f, progressY, textPaint)

        
        // Horizontal bar below percentage
        val barY = progressY + 15f
        canvas.drawRect(
            width / 2f - barWidth / 2f,
            barY,
            width / 2f + barWidth / 2f,
            barY + barHeight,
            barPaint
        )

        // Days left text (above percentage)
        textPaint.color = whiteColor
        textPaint.textSize = width * 0.055f
        textPaint.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        canvas.drawText("${daysRemaining}d left", width / 2f, progressY - 60f, textPaint)


        return bitmap
    }

    /**
     * Set the generated bitmap as device wallpaper
     */
    fun setWallpaper(bitmap: Bitmap) {
        val wallpaperManager = WallpaperManager.getInstance(context)
        wallpaperManager.setBitmap(bitmap)
    }

    companion object {
        /**
         * Calculate day of year from current date
         */
        fun getCurrentDayOfYear(): Int {
            return LocalDate.now().dayOfYear
        }
        
        /**
         * Get current year
         */
        fun getCurrentYear(): Int {
            return LocalDate.now().year
        }
    }
}
