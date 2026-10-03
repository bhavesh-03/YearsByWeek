package com.example.yearbyweeks.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.graphics.ColorUtils
import com.example.yearbyweeks.data.AppPreferences
import com.example.yearbyweeks.util.DateUtils
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.min

/** Shared renderer for previews and home-screen widgets. Only the surface is translucent. */
object WidgetRenderer {
    fun render(context: Context, width: Float, height: Float, date: LocalDate,
               weeks: Boolean = false, eventName: String? = null, eventDate: LocalDate? = null,
               opacityOverride: Float? = null): Bitmap {
        val prefs = AppPreferences(context)
        val dark = prefs.isDark(context)
        val density = context.resources.displayMetrics.density
        val w = width.coerceAtLeast(100f)
        val h = height.coerceAtLeast(60f)
        val bitmap = Bitmap.createBitmap((w * density).toInt(), (h * density).toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.scale(density, density)
        val ink = android.graphics.Color.parseColor(if (dark) "#F5F5F5" else "#111111")
        val muted = android.graphics.Color.parseColor(if (dark) "#A3A3A3" else "#666666")
        val accent = android.graphics.Color.parseColor(if (dark) "#F5F5F5" else "#111111")
        val faint = android.graphics.Color.parseColor(if (dark) "#303030" else "#E5E5E5")
        val surface = android.graphics.Color.parseColor(if (dark) "#171717" else "#FFFFFF")
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = ColorUtils.setAlphaComponent(surface,
            ((opacityOverride ?: prefs.opacity).coerceIn(0f, 1f) * 255).toInt())
        canvas.drawRoundRect(0f, 0f, w, h, 22f, 22f, paint)
        fun text(value: String, x: Float, y: Float, size: Float, color: Int, bold: Boolean = false, maxWidth: Float = w - 32f) {
            paint.color = color
            paint.textSize = size
            paint.typeface = Typeface.create(Typeface.DEFAULT, if (bold) Typeface.BOLD else Typeface.NORMAL)
            var fitted = value
            if (paint.measureText(fitted) > maxWidth) {
                while (fitted.isNotEmpty() && paint.measureText(fitted + "…") > maxWidth) fitted = fitted.dropLast(1)
                fitted += "…"
            }
            canvas.drawText(fitted, x, y, paint)
        }
        if (eventName != null && eventDate != null) {
            val days = DateUtils.daysUntil(eventDate, date)
            val rightWidth = w * 0.38f
            val leftWidth = w - rightWidth - 36f
            val number = if (days == 0) "Today" else abs(days).toString()
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            var numberSize = min(38f, h * 0.42f)
            paint.textSize = numberSize
            while (paint.measureText(number) > rightWidth && numberSize > 12) { numberSize--; paint.textSize = numberSize }
            text(eventName, 16f, h / 2f - 3f, 14f, ink, true, leftWidth)
            text(eventDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy")), 16f, h / 2f + 16f, 10f, muted, maxWidth = leftWidth)
            text(number, w - rightWidth - 12f, h / 2f + 3f, numberSize, accent, true, rightWidth)
            val label = when { days == 0 -> "Enjoy the moment"; days == -1 -> "day ago"; days < 0 -> "days ago"; days == 1 -> "day to go"; else -> "days to go" }
            text(label, w - rightWidth - 12f, h / 2f + 20f, 9f, muted, maxWidth = rightWidth)
        } else {
            val remaining = DateUtils.remainingDays(date)
            text(date.year.toString(), 16f, 31f, 18f, ink, true)
            val label = if (weeks) "${remaining / 7}w ${remaining % 7}d left" else "$remaining days left · incl. today"
            text(label, 16f, 51f, if (w < 180) 10f else 12f, muted)
            val total = if (weeks) DateUtils.totalWeeks(date.year) else DateUtils.totalDays(date.year)
            val current = if (weeks) DateUtils.weekOfYear(date) else date.dayOfYear
            val columns = if (weeks) 9 else 37
            val rows = ceil(total.toFloat() / columns).toInt()
            val cellW = (w - 32f) / columns
            val cellH = (h - 78f).coerceAtLeast(12f) / rows
            val radius = min(cellW, cellH) * 0.30f
            for (i in 0 until total) {
                paint.color = when { i + 1 < current -> faint; i + 1 == current -> accent; else -> ink }
                paint.style = if (i + 1 == current) Paint.Style.STROKE else Paint.Style.FILL
                paint.strokeWidth = (radius * 0.4f).coerceAtLeast(0.6f)
                canvas.drawCircle(16f + (i % columns + 0.5f) * cellW, 64f + (i / columns + 0.5f) * cellH, radius, paint)
            }
        }
        return bitmap
    }
}
