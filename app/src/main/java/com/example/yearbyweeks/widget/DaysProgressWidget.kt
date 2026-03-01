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
import com.example.yearbyweeks.MainActivity
import com.example.yearbyweeks.util.DateUtils
import java.time.LocalDate
import kotlin.math.ceil
import kotlin.math.min

/**
 * Motivational quotes about time - changes daily/weekly
 */
object TimeQuotes {
    private val quotes = listOf(
        "Time is what we want most, but what we use worst." to "William Penn",
        "The two most powerful warriors are patience and time." to "Leo Tolstoy",
        "Lost time is never found again." to "Benjamin Franklin",
        "Time flies over us, but leaves its shadow behind." to "Nathaniel Hawthorne",
        "The key is not to prioritize what's on your schedule, but to schedule your priorities." to "Stephen Covey",
        "Don't count the days, make the days count." to "Muhammad Ali",
        "Time is the most valuable thing a man can spend." to "Theophrastus",
        "Yesterday is gone. Tomorrow has not yet come. We have only today." to "Mother Teresa",
        "Time is a created thing. To say 'I don't have time' is to say 'I don't want to.'" to "Lao Tzu",
        "The bad news is time flies. The good news is you're the pilot." to "Michael Altshuler",
        "Time is the wisest counselor of all." to "Pericles",
        "Every moment is a fresh beginning." to "T.S. Eliot",
        "Time and tide wait for no man." to "Geoffrey Chaucer",
        "Your time is limited, don't waste it living someone else's life." to "Steve Jobs",
        "The only time you really live fully is from thirty to sixty." to "Hervey Allen",
        "Time is money." to "Benjamin Franklin",
        "Forever is composed of nows." to "Emily Dickinson",
        "Time you enjoy wasting is not wasted time." to "Marthe Troly-Curtin",
        "Better three hours too soon than a minute too late." to "William Shakespeare",
        "The future is something which everyone reaches at the rate of sixty minutes an hour." to "C.S. Lewis",
        "Time brings all things to pass." to "Aeschylus",
        "One day or day one. You decide." to "Paulo Coelho",
        "Make each day your masterpiece." to "John Wooden",
        "Time waits for no one." to "Folklore",
        "Today is the first day of the rest of your life." to "Charles Dederich",
        "Seize the day, put very little trust in tomorrow." to "Horace",
        "Time is the coin of your life. Be careful lest you let other people spend it for you." to "Carl Sandburg",
        "A year from now you may wish you had started today." to "Karen Lamb",
        "The present time has one advantage over every other – it is our own." to "Charles Caleb Colton",
        "Time is the longest distance between two places." to "Tennessee Williams",
        "How we spend our days is how we spend our lives." to "Annie Dillard",
        "Time flies when you're having fun." to "Albert Einstein",
        "In time, even a bear can learn to dance." to "Yiddish Proverb",
        "Time is a great teacher, but unfortunately it kills all its pupils." to "Hector Berlioz",
        "Use time wisely. Today is the day you've been waiting for." to "Unknown",
        "The future starts today, not tomorrow." to "Pope John Paul II",
        "Time passes whether you act or not." to "Robin Sharma",
        "An inch of time is an inch of gold." to "Chinese Proverb",
        "The best time to plant a tree was 20 years ago. The second best time is now." to "Chinese Proverb",
        "Time is the school in which we learn." to "Joan Didion",
        "They always say time changes things, but you actually have to change them yourself." to "Andy Warhol",
        "Time is a gift. Spend it wisely." to "Unknown",
        "Live as if you were to die tomorrow. Learn as if you were to live forever." to "Mahatma Gandhi",
        "Time is an illusion. Lunchtime doubly so." to "Douglas Adams",
        "The only reason for time is so that everything doesn't happen at once." to "Albert Einstein",
        "Waste your money and you're only out of money, but waste your time and you've lost a part of your life." to "Michael LeBoeuf",
        "You may delay, but time will not." to "Benjamin Franklin",
        "Time is the scarcest resource." to "Peter Drucker",
        "Don't wait. The time will never be just right." to "Napoleon Hill",
        "The time is always right to do what is right." to "Martin Luther King Jr.",
        "Time well spent is life well lived." to "Unknown"
    )

    /**
     * Get a quote that changes daily based on the day of the year
     */
    fun getDailyQuote(dayOfYear: Int): Pair<String, String> {
        val index = dayOfYear % quotes.size
        return quotes[index]
    }

    /**
     * Get a quote that changes weekly based on the week of the year
     */
    fun getWeeklyQuote(weekOfYear: Int): Pair<String, String> {
        val index = weekOfYear % quotes.size
        return quotes[index]
    }

    /**
     * Get today's motivational quote
     */
    fun getTodayQuote(): Pair<String, String> {
        val dayOfYear = LocalDate.now().dayOfYear
        return getDailyQuote(dayOfYear)
    }
}

/**
 * Days Progress Widget – Shows days of the year with percentage
 * 4x2 fixed size widget with daily motivational quote
 */
class DaysProgressWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val today = DateUtils.today()
        val year = today.year
        val totalDays = DateUtils.totalDays(year)
        val currentDay = DateUtils.dayOfYear(today)

        provideContent {
            GlanceTheme {
                val size = LocalSize.current
                WidgetContent(
                    size = size,
                    year = year,
                    totalDays = totalDays,
                    currentDay = currentDay,
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
    totalDays: Int,
    currentDay: Int,
    context: Context
) {
    val density = context.resources.displayMetrics.density

    val widgetWidth = size.width.value.coerceAtLeast(280f)
    val widgetHeight = size.height.value.coerceAtLeast(140f)

    val widgetBitmap = createDaysWidgetBitmap(
        year = year,
        totalDays = totalDays,
        currentDay = currentDay,
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
            contentDescription = "Days progress widget",
            modifier = GlanceModifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
    }
}

/* ---------- Days Widget Canvas ---------- */

private fun createDaysWidgetBitmap(
    year: Int,
    totalDays: Int,
    currentDay: Int,
    widthDp: Float,
    heightDp: Float,
    density: Float
): Bitmap {
    val widthPx = (widthDp * density).toInt().coerceAtLeast(100)
    val heightPx = (heightDp * density).toInt().coerceAtLeast(100)

    val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val padding = 16f * density
    val remainingDays = totalDays - currentDay
    val progressPercent = ((currentDay.toFloat() / totalDays) * 100).toInt()

    val bgColor = "#1C1C1E".toColorInt()
    val textWhite = "#FFFFFF".toColorInt()
    val textSecondary = "#8E8E93".toColorInt()
    val completedDot = "#3A3A3C".toColorInt()  // Dark for completed/passed days
    val currentDot = "#0A84FF".toColorInt()    // Blue for current day
    val remainingDot = "#FFFFFF".toColorInt()  // White for remaining days

    val bgPaint = Paint().apply {
        color = bgColor
        style = Paint.Style.FILL
    }
    canvas.drawRoundRect(0f, 0f, widthPx.toFloat(), heightPx.toFloat(), 22f * density, 22f * density, bgPaint)

    // Text sizes for 4x2 widget
    val yearTextSize = 18f * density
    val percentTextSize = 18f * density
    val daysLeftTextSize = 14f * density

    // Year text
    val yearPaint = Paint().apply {
        color = textWhite
        textSize = yearTextSize
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    // Percentage text
    val percentPaint = Paint().apply {
        color = textWhite
        textSize = percentTextSize
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    // Draw header - Year
    var yPos = padding + yearPaint.textSize
    canvas.drawText("$year", padding, yPos, yearPaint)

    // Draw percentage on the right
    val percentText = "$progressPercent%"
    val percentWidth = percentPaint.measureText(percentText)
    canvas.drawText(percentText, widthPx - padding - percentWidth, yPos, percentPaint)

    // Days left text
    yPos += 20f * density
    val daysLeftPaint = Paint().apply {
        color = textSecondary
        textSize = daysLeftTextSize
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        isAntiAlias = true
    }
    canvas.drawText("$remainingDays days left", padding, yPos, daysLeftPaint)

    // Grid area - use same padding as text for consistent alignment
    val gridTop = yPos + 12f * density
    val gridBottom = heightPx - padding
    val gridLeft = padding
    val gridRight = widthPx - padding

    // Grid configuration - more columns for days (since there are 365/366 days)
    val columns = 37  // ~10 days per row for 37 rows
    val rows = ceil(totalDays.toFloat() / columns).toInt()

    val gridWidth = gridRight - gridLeft
    val gridHeight = gridBottom - gridTop

    val cellWidth = gridWidth / columns
    val cellHeight = gridHeight / rows
    val dotRadius = (min(cellWidth, cellHeight) * 0.35f).coerceIn(1.5f * density, 4f * density)

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

    // Draw dots - full rows at top, incomplete row at bottom
    // Remaining days (white) at top, completed days (dark) at bottom
    for (i in 0 until totalDays) {
        val row = i / columns
        val col = i % columns

        // Calculate day number from bottom-up for coloring (day 1 at bottom, last day at top)
        val dayFromEnd = totalDays - 1 - i
        val dayNum = dayFromEnd + 1

        val cx = gridLeft + (col * cellWidth) + (cellWidth / 2)
        val cy = gridTop + (row * cellHeight) + (cellHeight / 2)

        val paint = when {
            dayNum < currentDay -> completedPaint
            dayNum == currentDay -> currentPaint
            else -> remainingPaint
        }

        canvas.drawCircle(cx, cy, dotRadius, paint)
    }

    return bitmap
}

/* ---------- Receiver ---------- */

class DaysProgressWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DaysProgressWidget()
}

