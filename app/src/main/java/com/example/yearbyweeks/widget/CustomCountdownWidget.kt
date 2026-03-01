package com.example.yearbyweeks.widget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.glance.*
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.layout.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import com.example.yearbyweeks.MainActivity

/**
 * Custom Countdown Widget - Modern 2x1 Design
 * Shows event name and days remaining in a sleek card layout
 */
class CustomCountdownWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    companion object {
        private const val PREFS_NAME = "custom_countdown_prefs"
        private const val KEY_EVENT_NAME = "event_name"
        private const val KEY_EVENT_DATE = "event_date"

        fun saveEvent(context: Context, widgetId: Int, eventName: String, eventDate: LocalDate) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putString("${KEY_EVENT_NAME}_$widgetId", eventName)
                .putString("${KEY_EVENT_DATE}_$widgetId", eventDate.toString())
                .apply()
        }

        fun getEvent(context: Context, widgetId: Int): Pair<String, LocalDate?> {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val name = prefs.getString("${KEY_EVENT_NAME}_$widgetId", "New Year") ?: "New Year"
            val dateStr = prefs.getString("${KEY_EVENT_DATE}_$widgetId", null)
            val date = dateStr?.let { LocalDate.parse(it) } ?: LocalDate.of(LocalDate.now().year + 1, 1, 1)
            return Pair(name, date)
        }

        fun deleteEvent(context: Context, widgetId: Int) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .remove("${KEY_EVENT_NAME}_$widgetId")
                .remove("${KEY_EVENT_DATE}_$widgetId")
                .apply()
        }
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val appWidgetId = id.hashCode()
        val (eventName, eventDate) = getEvent(context, appWidgetId)
        val daysRemaining = eventDate?.let {
            ChronoUnit.DAYS.between(LocalDate.now(), it).toInt()
        } ?: 0

        provideContent {
            GlanceTheme {
                val size = LocalSize.current
                CountdownContent(
                    size = size,
                    eventName = eventName,
                    daysRemaining = daysRemaining,
                    eventDate = eventDate,
                    context = LocalContext.current
                )
            }
        }
    }
}

@Composable
private fun CountdownContent(
    size: DpSize,
    eventName: String,
    daysRemaining: Int,
    eventDate: LocalDate?,
    context: Context
) {
    val density = context.resources.displayMetrics.density

    val widgetWidth = size.width.value.coerceAtLeast(200f)
    val widgetHeight = size.height.value.coerceAtLeast(80f)

    val widgetBitmap = createCountdownBitmap(
        eventName = eventName,
        daysRemaining = daysRemaining,
        eventDate = eventDate,
        widthDp = widgetWidth,
        heightDp = widgetHeight,
        density = density
    )

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .cornerRadius(20.dp)
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java)))
    ) {
        Image(
            provider = ImageProvider(widgetBitmap),
            contentDescription = "Countdown widget",
            modifier = GlanceModifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
    }
}

private fun createCountdownBitmap(
    eventName: String,
    daysRemaining: Int,
    eventDate: LocalDate?,
    widthDp: Float,
    heightDp: Float,
    density: Float
): Bitmap {
    val widthPx = (widthDp * density).toInt().coerceAtLeast(200)
    val heightPx = (heightDp * density).toInt().coerceAtLeast(80)

    val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Minimal padding for full width/height usage
    val horizontalPadding = 12f * density
    val verticalPadding = 8f * density

    // iOS-style colors - matching other widgets
    val bgColor = "#1C1C1E".toColorInt()           // iOS dark background
    val primaryText = "#FFFFFF".toColorInt()
    val secondaryText = "#8E8E93".toColorInt()     // iOS secondary label
    val accentColor = "#0A84FF".toColorInt()       // iOS blue accent
    val urgentColor = "#FF453A".toColorInt()       // iOS red for urgent

    // Background with Apple-style rounded corners
    val bgPaint = Paint().apply {
        color = bgColor
        style = Paint.Style.FILL
    }
    canvas.drawRoundRect(
        RectF(0f, 0f, widthPx.toFloat(), heightPx.toFloat()),
        20f * density, 20f * density, bgPaint
    )

    // Calculate available content area
    val contentWidth = widthPx - (horizontalPadding * 2)
    val contentHeight = heightPx - (verticalPadding * 2)
    val halfWidth = contentWidth / 2f

    // Scale text sizes based on widget height for better space usage
    val scaleFactor = (contentHeight / (70f * density)).coerceIn(0.8f, 1.3f)

    // Left side - Event info (smaller fonts)
    val eventNamePaint = Paint().apply {
        color = primaryText
        textSize = 13f * density * scaleFactor
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    val labelPaint = Paint().apply {
        color = secondaryText
        textSize = 10f * density * scaleFactor
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        isAntiAlias = true
    }

    // Truncate event name if too long - use more of the left half
    val maxEventWidth = halfWidth - horizontalPadding
    val truncatedName = truncateText(eventName, eventNamePaint, maxEventWidth)

    // Center content vertically
    val centerY = heightPx / 2f
    val eventNameY = centerY - 4f * density
    canvas.drawText(truncatedName, horizontalPadding, eventNameY, eventNamePaint)

    // Draw date label below event name
    val labelY = eventNameY + 20f * density * scaleFactor
    val dateText = eventDate?.let {
        val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
        it.format(formatter)
    } ?: "Set date"
    canvas.drawText(dateText, horizontalPadding, labelY, labelPaint)

    // Right side - Days count (aligned to right edge)
    val daysText = abs(daysRemaining).toString()
    val daysPaint = Paint().apply {
        color = when {
            daysRemaining < 0 -> urgentColor
            daysRemaining <= 7 -> urgentColor
            else -> accentColor
        }
        textSize = 42f * density * scaleFactor
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
        textAlign = Paint.Align.RIGHT
    }

    val daysLabelPaint = Paint().apply {
        color = secondaryText
        textSize = 11f * density * scaleFactor
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        isAntiAlias = true
        textAlign = Paint.Align.RIGHT
    }

    val rightX = widthPx - horizontalPadding

    // Draw days number - vertically centered
    canvas.drawText(daysText, rightX, centerY + 6f * density, daysPaint)

    // Draw "days" label
    val daysLabelText = when {
        daysRemaining < 0 -> "days ago"
        daysRemaining == 0 -> "TODAY!"
        daysRemaining == 1 -> "day left"
        else -> "days left"
    }
    canvas.drawText(daysLabelText, rightX, centerY + 22f * density * scaleFactor, daysLabelPaint)

    return bitmap
}

private fun truncateText(text: String, paint: Paint, maxWidth: Float): String {
    if (paint.measureText(text) <= maxWidth) return text

    var truncated = text
    while (truncated.isNotEmpty() && paint.measureText("$truncated...") > maxWidth) {
        truncated = truncated.dropLast(1)
    }
    return if (truncated.isEmpty()) text.take(3) + "..." else "$truncated..."
}

class CustomCountdownWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CustomCountdownWidget()
}
