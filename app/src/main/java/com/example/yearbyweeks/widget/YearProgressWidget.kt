package com.example.yearbyweeks.widget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
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
import androidx.work.*
import com.example.yearbyweeks.MainActivity
import com.example.yearbyweeks.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.WeekFields
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.ceil
import kotlin.math.min

/**
 * Year Progress Widget – Modern Minimal Design
 * Full canvas-based rendering for perfect control
 */
class YearProgressWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val today = DateUtils.today()
        val year = today.year
        val totalWeeks = DateUtils.totalWeeks(year)
        val currentWeek = DateUtils.weekOfYear(today)

        provideContent {
            GlanceTheme {
                val size = LocalSize.current
                WidgetContent(
                    size = size,
                    year = year,
                    totalWeeks = totalWeeks,
                    currentWeek = currentWeek,
                    context = LocalContext.current
                )
            }
        }
    }
}

/* ---------- Main Content ---------- */

@Composable
private fun WidgetContent(
    size: DpSize,
    year: Int,
    totalWeeks: Int,
    currentWeek: Int,
    context: Context
) {
    val density = context.resources.displayMetrics.density

    // Use actual widget size - let the system determine the size
    val widgetWidth = size.width.value.coerceAtLeast(140f)
    val widgetHeight = size.height.value.coerceAtLeast(140f)

    // Create full widget bitmap
    val widgetBitmap = createFullWidgetBitmap(
        year = year,
        totalWeeks = totalWeeks,
        currentWeek = currentWeek,
        widthDp = widgetWidth,
        heightDp = widgetHeight,
        density = density
    )

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .cornerRadius(24.dp)
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java)))
    ) {
        Image(
            provider = ImageProvider(widgetBitmap),
            contentDescription = "Year progress widget",
            modifier = GlanceModifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
    }
}

/* ---------- Full Widget Canvas ---------- */

private fun createFullWidgetBitmap(
    year: Int,
    totalWeeks: Int,
    currentWeek: Int,
    widthDp: Float,
    heightDp: Float,
    density: Float
): Bitmap {
    // Use actual dimensions - don't coerce to minimum
    val widthPx = (widthDp * density).toInt().coerceAtLeast(100)
    val heightPx = (heightDp * density).toInt().coerceAtLeast(100)

    val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val padding = 16f * density
    val remainingWeeks = totalWeeks - currentWeek

    // Apple-style colors - clean and minimal
    val bgColor = "#1C1C1E".toColorInt()            // iOS dark background
    val textWhite = "#FFFFFF".toColorInt()
    val textSecondary = "#8E8E93".toColorInt()      // iOS secondary label
    val completedDot = "#3A3A3C".toColorInt()       // Dark for completed/passed weeks
    val currentDot = "#0A84FF".toColorInt()         // iOS blue accent
    val remainingDot = "#FFFFFF".toColorInt()       // White for remaining weeks

    // Background with Apple-style rounded corners
    val bgPaint = Paint().apply {
        color = bgColor
        style = Paint.Style.FILL
    }
    canvas.drawRoundRect(0f, 0f, widthPx.toFloat(), heightPx.toFloat(), 22f * density, 22f * density, bgPaint)

    // Detect widget size - 2x2 is narrower
    val is2x2 = widthDp <= 160f

    // Text sizes - smaller for 2x2, normal for 3x2
    val yearTextSize = if (is2x2) 13f * density else 18f * density
    val weeksLeftTextSize = if (is2x2) 9f * density else 14f * density

    // Year text - SF Pro Display style, bold
    val yearPaint = Paint().apply {
        color = textWhite
        textSize = yearTextSize
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    // Draw header - Year (bold, white)
    var yPos = padding + yearPaint.textSize
    canvas.drawText("$year", padding, yPos, yearPaint)


    // Weeks left - secondary label style
    yPos += if (is2x2) 16f * density else 20f * density
    val weeksLeftPaint = Paint().apply {
        color = textSecondary
        textSize = weeksLeftTextSize
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        isAntiAlias = true
    }
    canvas.drawText("$remainingWeeks weeks left", padding, yPos, weeksLeftPaint)

    // Grid area - use full remaining space with consistent padding
    val gridTop = yPos + 12f * density
    val gridBottom = heightPx - padding
    val gridLeft = padding
    val gridRight = widthPx - padding

    // Grid configuration
    val columns = 9
    val rows = ceil(totalWeeks.toFloat() / columns).toInt()

    val gridWidth = gridRight - gridLeft
    val gridHeight = gridBottom - gridTop

    val cellWidth = gridWidth / columns
    val cellHeight = gridHeight / rows

    // Fixed dot radius - same for ALL dots
    val dotRadius = (min(cellWidth, cellHeight) * 0.32f).coerceIn(3.5f * density, 10f * density)

    // Single paint for each type - all use same radius
    val completedPaint = Paint().apply {
        color = completedDot
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    val currentPaint = Paint().apply {
        color = currentDot
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    val remainingPaint = Paint().apply {
        color = remainingDot
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    // Draw all dots with SAME size - start from bottom (remaining weeks at top, completed at bottom)
    // Layout: full rows at top, incomplete row at bottom
    for (i in 0 until totalWeeks) {
        val row = i / columns
        val col = i % columns

        // Calculate day number from bottom-up for coloring (day 1 at bottom, last day at top)
        val dayFromEnd = totalWeeks - 1 - i
        val weekNum = dayFromEnd + 1

        val cx = gridLeft + (col * cellWidth) + (cellWidth / 2)
        val cy = gridTop + (row * cellHeight) + (cellHeight / 2)

        val paint = when {
            weekNum < currentWeek -> completedPaint
            weekNum == currentWeek -> currentPaint
            else -> remainingPaint
        }

        // All dots use the same dotRadius
        canvas.drawCircle(cx, cy, dotRadius, paint)
    }


    return bitmap
}

/* ---------- Receiver ---------- */

class YearProgressWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = YearProgressWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetRefreshWorker.enqueue(context)
    }
}

/* ---------- Update Helper ---------- */

object YearProgressWidgetUpdater {
    suspend fun updateAll(context: Context) {
        GlanceAppWidgetManager(context)
            .getGlanceIds(YearProgressWidget::class.java)
            .forEach { id ->
                YearProgressWidget().update(context, id)
            }
    }
}

/* ---------- Worker ---------- */

class WidgetRefreshWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.Default) {
        YearProgressWidgetUpdater.updateAll(applicationContext)
        Result.success()
    }

    companion object {
        private const val WORK_NAME = "year_progress_widget_refresh"

        fun enqueue(context: Context) {
            val zone = ZoneId.systemDefault()
            val now = ZonedDateTime.now(zone)
            val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek
            val daysUntilNextStart = ((firstDayOfWeek.value - now.dayOfWeek.value + 7) % 7)
                .let { if (it == 0) 7 else it }
            val nextWeekStart = now.toLocalDate()
                .plusDays(daysUntilNextStart.toLong())
                .atStartOfDay(zone)
            val delay = java.time.Duration.between(now, nextWeekStart)

            val request = PeriodicWorkRequestBuilder<WidgetRefreshWorker>(
                7, TimeUnit.DAYS
            )
                .setInitialDelay(delay)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }
    }
}

