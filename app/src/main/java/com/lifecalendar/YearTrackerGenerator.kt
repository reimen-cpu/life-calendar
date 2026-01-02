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
    
    // Mint green color matching the reference image
    private val mintGreen = Color.parseColor("#7FFFD4")
    
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
        
        // Calculate dynamic padding based on text position
        val topSpace = if (isDaysTop) 0.25f else 0.15f
        val bottomSpace = if (isDaysTop) 0.15f else 0.25f
        
        val paddingTop = height * topSpace
        val paddingBottom = height * bottomSpace
        val paddingHorizontal = width * 0.06f

        val gridWidth = width - (2 * paddingHorizontal)
        val gridHeight = height - paddingTop - paddingBottom

        // Calculate cell and dot sizes
        val cellWidth = gridWidth / columns
        val cellHeight = gridHeight / rows
        val dotRadius = min(cellWidth, cellHeight) * 0.35f
        val dotSpacing = dotRadius * 0.15f

        // Paint for remaining days (colored dots - mint green)
        val coloredPaint = Paint().apply {
            isAntiAlias = true
            color = mintGreen
            alpha = 255
            style = Paint.Style.FILL
        }

        // Paint for passed days (black/very dark dots - "lost forever")
        val passedPaint = Paint().apply {
            isAntiAlias = true
            color = Color.parseColor("#1A1A1A")  // Very dark gray, almost black
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

                val isPassed = dayCounter < dayOfYear

                if (isPassed) {
                    // Day has passed - show as dark/black (lost forever)
                    canvas.drawCircle(x, y, dotRadius - dotSpacing, passedPaint)
                } else {
                    // Day remaining - show as colored (mint green)
                    canvas.drawCircle(x, y, dotRadius - dotSpacing, coloredPaint)
                }

                dayCounter++
            }
        }

        // Draw days remaining text
        val textPaint = Paint().apply {
            isAntiAlias = true
            color = mintGreen
            typeface = Typeface.create("sans-serif-black", Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        
        // Days remaining number - bold and compact
        textPaint.textSize = width * 0.08f
        
        val textY = if (isDaysTop) {
            // Position at top
            height * 0.12f
        } else {
            // Position at bottom
            height - height * 0.10f
        }
        
        canvas.drawText(daysRemaining.toString(), width / 2f, textY, textPaint)

        // "days left in YEAR" label - smaller
        textPaint.textSize = width * 0.032f
        textPaint.alpha = 200
        textPaint.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        
        val labelY = textY + width * 0.045f
        canvas.drawText("days left in $year", width / 2f, labelY, textPaint)

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
