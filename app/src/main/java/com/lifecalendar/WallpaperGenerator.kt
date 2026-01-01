package com.lifecalendar

import android.app.WallpaperManager
import android.content.Context
import android.graphics.*
import android.util.DisplayMetrics
import android.view.WindowManager
import kotlin.math.min

/**
 * Generates Life Calendar wallpaper bitmap
 * Shows weeks lived (filled dots) vs remaining weeks (hollow dots)
 */
class WallpaperGenerator(private val context: Context) {

    private val weeksPerYear = 52
    private val defaultLifeExpectancy = 80

    /**
     * Generate wallpaper bitmap for the device screen
     */
    fun generateWallpaper(weeksLived: Int, lifeExpectancy: Int = defaultLifeExpectancy): Bitmap {
        // Get screen dimensions
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getRealMetrics(metrics)
        
        val width = metrics.widthPixels
        val height = metrics.heightPixels

        return generateBitmap(width, height, weeksLived, lifeExpectancy)
    }

    /**
     * Generate bitmap with specified dimensions
     */
    fun generateBitmap(
        width: Int,
        height: Int,
        weeksLived: Int,
        lifeExpectancy: Int = defaultLifeExpectancy
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Fill background with pure black
        canvas.drawColor(Color.BLACK)

        val totalYears = lifeExpectancy
        
        // Calculate padding
        val paddingTop = height * 0.15f
        val paddingBottom = height * 0.08f
        val paddingLeft = width * 0.12f
        val paddingRight = width * 0.08f

        val gridWidth = width - paddingLeft - paddingRight
        val gridHeight = height - paddingTop - paddingBottom

        // Calculate cell and dot sizes
        val cellWidth = gridWidth / weeksPerYear
        val cellHeight = gridHeight / totalYears
        val dotRadius = min(cellWidth, cellHeight) * 0.35f
        val dotSpacing = dotRadius * 0.3f

        // Paint for text
        val textPaint = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        }

        // Draw "LIFE CALENDAR" title
        textPaint.textSize = width * 0.028f
        textPaint.alpha = 230
        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText("LIFE CALENDAR", width - paddingRight, paddingTop * 0.45f, textPaint)

        // Draw "WEEK OF THE YEAR" label
        textPaint.textSize = width * 0.018f
        textPaint.alpha = 128
        textPaint.textAlign = Paint.Align.LEFT
        canvas.drawText("WEEK OF THE YEAR", paddingLeft, paddingTop * 0.45f, textPaint)

        // Draw "YEAR OF YOUR LIFE" label (rotated)
        canvas.save()
        canvas.translate(paddingLeft * 0.35f, paddingTop + gridHeight / 2)
        canvas.rotate(-90f)
        textPaint.textAlign = Paint.Align.CENTER
        canvas.drawText("YEAR OF YOUR LIFE", 0f, 0f, textPaint)
        canvas.restore()

        // Paint for filled dots (lived weeks)
        val filledPaint = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
            alpha = 230
            style = Paint.Style.FILL
        }

        // Paint for hollow dots (remaining weeks)
        val hollowPaint = Paint().apply {
            isAntiAlias = true
            color = Color.WHITE
            alpha = 64
            style = Paint.Style.STROKE
            strokeWidth = (dotRadius * 0.15f).coerceAtLeast(1f)
        }

        // Draw grid of dots
        var weekCounter = 0
        for (year in 0 until totalYears) {
            for (week in 0 until weeksPerYear) {
                val x = paddingLeft + (week * cellWidth) + (cellWidth / 2)
                val y = paddingTop + (year * cellHeight) + (cellHeight / 2)

                val isLived = weekCounter < weeksLived

                if (isLived) {
                    canvas.drawCircle(x, y, dotRadius - dotSpacing, filledPaint)
                } else {
                    canvas.drawCircle(x, y, dotRadius - dotSpacing, hollowPaint)
                }

                weekCounter++
            }
        }

        // Draw year markers (every 10 years)
        textPaint.textSize = width * 0.022f
        textPaint.alpha = 102
        textPaint.textAlign = Paint.Align.RIGHT

        for (year in 10..totalYears step 10) {
            val y = paddingTop + (year * cellHeight)
            canvas.drawText(
                year.toString(),
                paddingLeft - width * 0.02f,
                y + (cellHeight * 0.3f),
                textPaint
            )
        }

        return bitmap
    }

    /**
     * Set the generated bitmap as device wallpaper
     */
    fun setWallpaper(bitmap: Bitmap) {
        val wallpaperManager = WallpaperManager.getInstance(context)
        wallpaperManager.setBitmap(bitmap)
    }
}
