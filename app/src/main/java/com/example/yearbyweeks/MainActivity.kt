package com.example.yearbyweeks

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.example.yearbyweeks.ui.theme.YearByWeeksTheme
import com.example.yearbyweeks.util.DateUtils
import com.example.yearbyweeks.widget.CustomCountdownWidgetReceiver
import com.example.yearbyweeks.widget.DaysProgressWidgetReceiver
import com.example.yearbyweeks.widget.YearProgressWidgetReceiver
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.min

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YearByWeeksTheme(darkTheme = true, dynamicColor = false) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF000000)
                ) {
                    YearProgressApp()
                }
            }
        }
    }
}

@Composable
fun YearProgressApp() {
    val context = LocalContext.current
    val today = remember { DateUtils.today() }
    val year = today.year
    val totalWeeks = remember { DateUtils.totalWeeks(year) }
    val currentWeek = remember { DateUtils.weekOfYear(today) }
    val remainingWeeks = totalWeeks - currentWeek
    val weeksPercent = ((currentWeek.toFloat() / totalWeeks) * 100).toInt()
    val totalDays = remember { DateUtils.totalDays(year) }
    val currentDay = remember { DateUtils.dayOfYear(today) }
    val remainingDays = totalDays - currentDay
    val daysPercent = ((currentDay.toFloat() / totalDays) * 100).toInt()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 70.dp, bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App Title
        Text(
            text = "YearByWeeks",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            letterSpacing = (-1).sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Year
        Text(
            text = "$year",
            fontSize = 20.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF8E8E93)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Weeks Widget Section
        WidgetSection(
            title = "Weeks",
            value = "$remainingWeeks",
            subtitle = "weeks remaining",
            percent = weeksPercent,
            widgetPreview = {
                WeeksWidgetPreview(
                    year = year,
                    totalWeeks = totalWeeks,
                    currentWeek = currentWeek
                )
            },
            onAddWidget = {
                addWidget(context, YearProgressWidgetReceiver::class.java)
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Days Widget Section
        WidgetSection(
            title = "Days",
            value = "$remainingDays",
            subtitle = "days remaining",
            percent = daysPercent,
            widgetPreview = {
                DaysWidgetPreview(
                    year = year,
                    totalDays = totalDays,
                    currentDay = currentDay
                )
            },
            onAddWidget = {
                addWidget(context, DaysProgressWidgetReceiver::class.java)
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Event Countdown Widget Section
        val sampleEventName = "New Year"
        val sampleEventDate = LocalDate.of(year + 1, 1, 1)
        val sampleDaysRemaining = ChronoUnit.DAYS.between(today, sampleEventDate).toInt()

        CountdownWidgetSection(
            eventName = sampleEventName,
            eventDate = sampleEventDate,
            daysRemaining = sampleDaysRemaining,
            widgetPreview = {
                CountdownWidgetPreview(
                    eventName = sampleEventName,
                    eventDate = sampleEventDate,
                    daysRemaining = sampleDaysRemaining
                )
            },
            onAddWidget = {
                addWidget(context, CustomCountdownWidgetReceiver::class.java)
            }
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Footer
        Text(
            text = "Track your year progress",
            fontSize = 14.sp,
            color = Color(0xFF48484A),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun WidgetSection(
    title: String,
    value: String,
    subtitle: String,
    percent: Int,
    widgetPreview: @Composable () -> Unit,
    onAddWidget: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF8E8E93),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = value,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = (-2).sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = subtitle,
                        fontSize = 16.sp,
                        color = Color(0xFF8E8E93)
                    )
                }
            }
            Text(
                text = "$percent%",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0A84FF)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Widget Preview
        widgetPreview()

        Spacer(modifier = Modifier.height(16.dp))

        // Add Widget Button
        Button(
            onClick = onAddWidget,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1C1C1E),
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Add to Home Screen",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun WeeksWidgetPreview(
    year: Int,
    totalWeeks: Int,
    currentWeek: Int
) {
    val density = LocalDensity.current.density
    // Match actual widget: 2x2 cells = 140dp x 140dp (square)
    val widgetSize = 140f
    val bitmap = remember(year, totalWeeks, currentWeek) {
        createWeeksPreviewBitmap(year, totalWeeks, currentWeek, widgetSize, widgetSize, density)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Weeks widget preview",
            modifier = Modifier
                .size(widgetSize.dp)
                .clip(RoundedCornerShape(24.dp)),
            contentScale = ContentScale.FillBounds
        )
    }
}

@Composable
fun DaysWidgetPreview(
    year: Int,
    totalDays: Int,
    currentDay: Int
) {
    val density = LocalDensity.current.density
    // Larger preview for better visibility in app (actual widget is 280x140)
    val widgetWidth = 320f
    val widgetHeight = 180f
    val bitmap = remember(year, totalDays, currentDay) {
        createDaysPreviewBitmap(year, totalDays, currentDay, widgetWidth, widgetHeight, density)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Days widget preview",
            modifier = Modifier
                .width(widgetWidth.dp)
                .height(widgetHeight.dp)
                .clip(RoundedCornerShape(24.dp)),
            contentScale = ContentScale.FillBounds
        )
    }
}

private fun <T> addWidget(context: Context, receiverClass: Class<T>) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val widgetProvider = ComponentName(context, receiverClass)

        if (appWidgetManager.isRequestPinAppWidgetSupported) {
            appWidgetManager.requestPinAppWidget(widgetProvider, null, null)
        }
    }
}

@Composable
fun CountdownWidgetSection(
    eventName: String,
    eventDate: LocalDate,
    daysRemaining: Int,
    widgetPreview: @Composable () -> Unit,
    onAddWidget: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = "Event Countdown",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF8E8E93),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "${abs(daysRemaining)}",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = (-2).sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (daysRemaining >= 0) "days until $eventName" else "days since $eventName",
                        fontSize = 16.sp,
                        color = Color(0xFF8E8E93)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Widget Preview
        widgetPreview()

        Spacer(modifier = Modifier.height(16.dp))

        // Add Widget Button
        Button(
            onClick = onAddWidget,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1C1C1E),
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Add to Home Screen",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun CountdownWidgetPreview(
    eventName: String,
    eventDate: LocalDate,
    daysRemaining: Int
) {
    val density = LocalDensity.current.density
    // 2x1 widget preview
    val widgetWidth = 200f
    val widgetHeight = 80f
    val bitmap = remember(eventName, eventDate, daysRemaining) {
        createCountdownPreviewBitmap(eventName, eventDate, daysRemaining, widgetWidth, widgetHeight, density)
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Countdown widget preview",
            modifier = Modifier
                .width(widgetWidth.dp)
                .height(widgetHeight.dp)
                .clip(RoundedCornerShape(22.dp)),
            contentScale = ContentScale.FillBounds
        )
    }
}

// Countdown Preview Bitmap - matches CustomCountdownWidget
private fun createCountdownPreviewBitmap(
    eventName: String,
    eventDate: LocalDate,
    daysRemaining: Int,
    widthDp: Float,
    heightDp: Float,
    density: Float
): Bitmap {
    val widthPx = (widthDp * density).toInt()
    val heightPx = (heightDp * density).toInt()

    val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val padding = 16f * density
    val halfWidth = widthPx / 2f

    // iOS-style colors
    val bgColor = "#1C1C1E".toColorInt()
    val primaryText = "#FFFFFF".toColorInt()
    val secondaryText = "#8E8E93".toColorInt()
    val accentColor = "#0A84FF".toColorInt()
    val urgentColor = "#FF453A".toColorInt()

    // Background
    val bgPaint = Paint().apply {
        color = bgColor
        style = Paint.Style.FILL
    }
    canvas.drawRoundRect(0f, 0f, widthPx.toFloat(), heightPx.toFloat(), 22f * density, 22f * density, bgPaint)

    // Event name
    val eventNamePaint = Paint().apply {
        color = primaryText
        textSize = 15f * density
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    val labelPaint = Paint().apply {
        color = secondaryText
        textSize = 11f * density
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        isAntiAlias = true
    }

    // Truncate event name if too long
    val maxEventWidth = halfWidth - padding * 1.5f
    var truncatedName = eventName
    while (truncatedName.isNotEmpty() && eventNamePaint.measureText("$truncatedName...") > maxEventWidth) {
        truncatedName = truncatedName.dropLast(1)
    }
    if (truncatedName != eventName && truncatedName.isNotEmpty()) {
        truncatedName = "$truncatedName..."
    }

    val eventNameY = heightPx / 2f - 2f * density
    canvas.drawText(truncatedName, padding, eventNameY, eventNamePaint)

    val labelY = eventNameY + 18f * density
    val dateText = eventDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
    canvas.drawText(dateText, padding, labelY, labelPaint)

    // Days count
    val daysText = abs(daysRemaining).toString()
    val daysPaint = Paint().apply {
        color = when {
            daysRemaining < 0 -> urgentColor
            daysRemaining <= 7 -> urgentColor
            else -> accentColor
        }
        textSize = 38f * density
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
        textAlign = Paint.Align.RIGHT
    }

    val daysLabelPaint = Paint().apply {
        color = secondaryText
        textSize = 10f * density
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        isAntiAlias = true
        textAlign = Paint.Align.RIGHT
    }

    val rightX = widthPx - padding
    canvas.drawText(daysText, rightX, heightPx / 2f + 4f * density, daysPaint)

    val daysLabelText = when {
        daysRemaining < 0 -> "days ago"
        daysRemaining == 0 -> "TODAY!"
        daysRemaining == 1 -> "day left"
        else -> "days left"
    }
    canvas.drawText(daysLabelText, rightX, heightPx / 2f + 20f * density, daysLabelPaint)

    return bitmap
}

// Weeks Preview Bitmap - matches YearProgressWidget exactly
private fun createWeeksPreviewBitmap(
    year: Int,
    totalWeeks: Int,
    currentWeek: Int,
    widthDp: Float,
    heightDp: Float,
    density: Float
): Bitmap {
    val widthPx = (widthDp * density).toInt()
    val heightPx = (heightDp * density).toInt()

    val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val padding = 16f * density
    val remainingWeeks = totalWeeks - currentWeek

    val bgColor = "#1C1C1E".toColorInt()
    val textWhite = "#FFFFFF".toColorInt()
    val textSecondary = "#8E8E93".toColorInt()
    val completedDot = "#3A3A3C".toColorInt()
    val currentDot = "#0A84FF".toColorInt()
    val remainingDot = "#FFFFFF".toColorInt()

    val bgPaint = Paint().apply {
        color = bgColor
        style = Paint.Style.FILL
    }
    canvas.drawRoundRect(0f, 0f, widthPx.toFloat(), heightPx.toFloat(), 22f * density, 22f * density, bgPaint)

    // Detect widget size - 2x2 is narrower (match widget logic)
    val is2x2 = widthDp <= 160f

    // Text sizes - smaller for 2x2, normal for 3x2 (match widget)
    val yearTextSize = if (is2x2) 13f * density else 18f * density
    val weeksLeftTextSize = if (is2x2) 9f * density else 14f * density

    val yearPaint = Paint().apply {
        color = textWhite
        textSize = yearTextSize
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    var yPos = padding + yearPaint.textSize
    canvas.drawText("$year", padding, yPos, yearPaint)

    yPos += if (is2x2) 16f * density else 20f * density
    val weeksLeftPaint = Paint().apply {
        color = textSecondary
        textSize = weeksLeftTextSize
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        isAntiAlias = true
    }
    canvas.drawText("$remainingWeeks weeks left", padding, yPos, weeksLeftPaint)

    val gridTop = yPos + 12f * density
    val gridBottom = heightPx - padding
    val gridLeft = padding
    val gridRight = widthPx - padding

    val columns = 9
    val rows = ceil(totalWeeks.toFloat() / columns).toInt()

    val gridWidth = gridRight - gridLeft
    val gridHeight = gridBottom - gridTop

    val cellWidth = gridWidth / columns
    val cellHeight = gridHeight / rows
    val dotRadius = (min(cellWidth, cellHeight) * 0.32f).coerceIn(3.5f * density, 10f * density)

    val completedPaint = Paint().apply { color = completedDot; isAntiAlias = true; style = Paint.Style.FILL }
    val currentPaint = Paint().apply { color = currentDot; isAntiAlias = true; style = Paint.Style.FILL }
    val remainingPaint = Paint().apply { color = remainingDot; isAntiAlias = true; style = Paint.Style.FILL }

    for (i in 0 until totalWeeks) {
        val row = i / columns
        val col = i % columns

        val weekFromEnd = totalWeeks - 1 - i
        val weekNum = weekFromEnd + 1

        val cx = gridLeft + (col * cellWidth) + (cellWidth / 2)
        val cy = gridTop + (row * cellHeight) + (cellHeight / 2)

        val paint = when {
            weekNum < currentWeek -> completedPaint
            weekNum == currentWeek -> currentPaint
            else -> remainingPaint
        }

        canvas.drawCircle(cx, cy, dotRadius, paint)
    }

    return bitmap
}

// Days Preview Bitmap
private fun createDaysPreviewBitmap(
    year: Int,
    totalDays: Int,
    currentDay: Int,
    widthDp: Float,
    heightDp: Float,
    density: Float
): Bitmap {
    val widthPx = (widthDp * density).toInt()
    val heightPx = (heightDp * density).toInt()

    val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val padding = 16f * density
    val remainingDays = totalDays - currentDay
    val progressPercent = ((currentDay.toFloat() / totalDays) * 100).toInt()

    val bgColor = "#1C1C1E".toColorInt()
    val textWhite = "#FFFFFF".toColorInt()
    val textSecondary = "#8E8E93".toColorInt()
    val completedDot = "#3A3A3C".toColorInt()  // Dark for completed/passed days
    val currentDot = "#0A84FF".toColorInt()
    val remainingDot = "#FFFFFF".toColorInt()  // White for remaining days

    val bgPaint = Paint().apply {
        color = bgColor
        style = Paint.Style.FILL
    }
    canvas.drawRoundRect(0f, 0f, widthPx.toFloat(), heightPx.toFloat(), 22f * density, 22f * density, bgPaint)

    val yearPaint = Paint().apply {
        color = textWhite
        textSize = 18f * density
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    val percentPaint = Paint().apply {
        color = textWhite
        textSize = 18f * density
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    var yPos = padding + yearPaint.textSize
    canvas.drawText("$year", padding, yPos, yearPaint)

    val percentText = "$progressPercent%"
    val percentWidth = percentPaint.measureText(percentText)
    canvas.drawText(percentText, widthPx - padding - percentWidth, yPos, percentPaint)

    yPos += 20f * density
    val daysLeftPaint = Paint().apply {
        color = textSecondary
        textSize = 14f * density
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        isAntiAlias = true
    }
    canvas.drawText("$remainingDays days left", padding, yPos, daysLeftPaint)

    val gridTop = yPos + 12f * density
    val gridBottom = heightPx - padding
    val gridLeft = padding
    val gridRight = widthPx - padding

    val columns = 37
    val rows = ceil(totalDays.toFloat() / columns).toInt()

    val gridWidth = gridRight - gridLeft
    val gridHeight = gridBottom - gridTop

    val cellWidth = gridWidth / columns
    val cellHeight = gridHeight / rows
    val dotRadius = (min(cellWidth, cellHeight) * 0.35f).coerceIn(1.5f * density, 4f * density)

    val completedPaint = Paint().apply { color = completedDot; isAntiAlias = true; style = Paint.Style.FILL }
    val currentPaint = Paint().apply { color = currentDot; isAntiAlias = true; style = Paint.Style.FILL }
    val remainingPaint = Paint().apply { color = remainingDot; isAntiAlias = true; style = Paint.Style.FILL }

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

