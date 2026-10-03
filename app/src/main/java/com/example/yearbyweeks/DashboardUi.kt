package com.example.yearbyweeks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yearbyweeks.data.Countdown
import com.example.yearbyweeks.util.DateUtils
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.min

@Composable
private fun ScreenColumn(content: @Composable ColumnScope.() -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = 600.dp).fillMaxWidth().verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp).padding(top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp), content = content)
    }
}

@Composable
private fun Eyebrow(text: String) {
    Text(text.uppercase(), style = MaterialTheme.typography.labelSmall, letterSpacing = 1.5.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun Panel(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier, shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp), content = content)
    }
}

@Composable
fun YearDashboard(today: LocalDate, onPin: (Boolean) -> Unit) {
    var weeks by rememberSaveable { mutableStateOf(true) }
    val colors = MaterialTheme.colorScheme
    val remaining = DateUtils.remainingDays(today)
    ScreenColumn {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Eyebrow(today.format(DateTimeFormatter.ofPattern("EEEE, d MMMM")))
            Text("A year in perspective.", fontSize = 30.sp, lineHeight = 36.sp,
                fontWeight = FontWeight.SemiBold, letterSpacing = (-1).sp)
        }
        Panel(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Eyebrow("Time remaining")
                Text("${today.year}", style = MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("$remaining", fontSize = 88.sp, lineHeight = 90.sp, letterSpacing = (-6).sp, fontWeight = FontWeight.Medium)
                Column(Modifier.weight(1f).padding(bottom = 12.dp)) {
                    Text("days left", style = MaterialTheme.typography.titleMedium)
                    Text("including today", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
            }
            Box(Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(colors.outlineVariant)) {
                Box(Modifier.fillMaxWidth(DateUtils.progress(today)).fillMaxHeight().background(colors.primary))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${DateUtils.elapsedDays(today)} days complete", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                Text("${(DateUtils.progress(today) * 100).toInt()}%", style = MaterialTheme.typography.labelLarge)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("The big picture", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(selected = weeks, onClick = { weeks = true }, label = { Text("Weeks") })
                    FilterChip(selected = !weeks, onClick = { weeks = false }, label = { Text("Days") })
                }
            }
            val total = if (weeks) DateUtils.totalWeeks(today.year) else DateUtils.totalDays(today.year)
            val current = if (weeks) DateUtils.weekOfYear(today) else today.dayOfYear
            DotGrid(total, current, weeks)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                DotLegend("Complete", 0)
                DotLegend("Now", 1)
                DotLegend("Ahead", 2)
            }
            Text(if (weeks) "${remaining / 7} full weeks + ${remaining % 7} days to go. Each dot is a week from January 1; the last is a partial week."
                else "One dot, one day. A little reminder to make this one count.",
                style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            OutlinedButton(onClick = { onPin(weeks) }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                shape = RoundedCornerShape(14.dp), border = BorderStroke(1.dp, colors.outlineVariant)) {
                Text(if (weeks) "Add weeks widget" else "Add days widget")
            }
        }
    }
}

@Composable
private fun DotGrid(total: Int, current: Int, weeks: Boolean) {
    val colors = MaterialTheme.colorScheme
    Canvas(Modifier.fillMaxWidth().height(if (weeks) 112.dp else 164.dp)
        .semantics { contentDescription = "${if (weeks) "Week" else "Day"} $current of $total. Outlined dot marks the current ${if (weeks) "week" else "day"}." }) {
        val columns = if (weeks) 13 else 26
        val rows = ceil(total.toFloat() / columns).toInt()
        val cellW = size.width / columns
        val cellH = size.height / rows
        val radius = min(cellW, cellH) * 0.25f
        for (i in 0 until total) {
            val center = Offset((i % columns + .5f) * cellW, (i / columns + .5f) * cellH)
            if (i + 1 == current) drawCircle(colors.onSurface, radius, center, style = Stroke(radius * .38f))
            else drawCircle(if (i + 1 < current) colors.outlineVariant else colors.onSurface, radius, center)
        }
    }
}

@Composable
private fun DotLegend(label: String, kind: Int) {
    val colors = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Canvas(Modifier.size(8.dp)) {
            if (kind == 1) drawCircle(colors.onSurface, size.width * .35f, style = Stroke(1.dp.toPx()))
            else drawCircle(if (kind == 0) colors.outlineVariant else colors.onSurface)
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
    }
}

@Composable
fun CountdownDashboard(today: LocalDate, events: List<Countdown>, onNew: () -> Unit,
                       onEdit: (Countdown) -> Unit, onDelete: (Countdown) -> Unit, onPin: (Countdown) -> Unit) {
    val colors = MaterialTheme.colorScheme
    ScreenColumn {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Eyebrow("Your milestones")
            Text("Worth the wait.", fontSize = 34.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-1).sp)
            Text("Keep the dates that matter close.", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
        }
        Button(onClick = onNew, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp)) {
            Text("+  New countdown", fontWeight = FontWeight.SemiBold)
        }
        if (events.isEmpty()) {
            Panel(Modifier.fillMaxWidth()) {
                Text("—", fontSize = 56.sp, color = colors.onSurfaceVariant)
                Text("Good things take time.", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium)
                Text("A birthday. A big trip. A new beginning.\nGive your next moment a place here.",
                    style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            }
        }
        events.sortedWith(compareBy<Countdown> { it.date < today }.thenBy { abs(DateUtils.daysUntil(it.date, today)) }).forEach { event ->
            key(event.id) {
                var menu by remember { mutableStateOf(false) }
                val days = DateUtils.daysUntil(event.date, today)
                Panel(Modifier.fillMaxWidth()) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(event.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                                maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text(event.date.format(DateTimeFormatter.ofPattern("d MMM yyyy")),
                                style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                        }
                        Box {
                            TextButton(onClick = { menu = true }, modifier = Modifier.semantics { contentDescription = "Options for ${event.name}" }) {
                                Text("•••", letterSpacing = 2.sp)
                            }
                            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                                DropdownMenuItem(text = { Text("Edit") }, onClick = { menu = false; onEdit(event) })
                                DropdownMenuItem(text = { Text("Delete") }, onClick = { menu = false; onDelete(event) })
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(if (days == 0) "Today" else abs(days).toString(), fontSize = 60.sp, lineHeight = 68.sp,
                            fontWeight = FontWeight.Medium, letterSpacing = (-2).sp)
                        if (days != 0) Text(if (days < 0) "${if (days == -1) "day" else "days"} ago" else "${if (days == 1) "day" else "days"} to go",
                            Modifier.padding(bottom = 10.dp), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                    }
                    HorizontalDivider(color = colors.outlineVariant)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(if (days < 0) "A moment to remember" else if (days == 0) "Make it a good one" else "Something to look forward to",
                            style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant, modifier = Modifier.weight(1f))
                        TextButton(onClick = { onPin(event) }) { Text("Add widget  +") }
                    }
                }
            }
        }
        Text("Tomorrow is 1 day away. Past dates count days since.", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
    }
}

@Composable
fun AppearanceDashboard(today: LocalDate, theme: String, opacity: Float, onTheme: (String) -> Unit,
                        onOpacity: (Float) -> Unit, onOpacityFinished: () -> Unit, preview: @Composable () -> Unit) {
    val colors = MaterialTheme.colorScheme
    ScreenColumn {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Eyebrow("The details")
            Text("Make it yours.", fontSize = 34.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-1).sp)
            Text("Less noise. More clarity.", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
        }
        Panel(Modifier.fillMaxWidth()) {
            Text("Appearance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("System", "Light", "Dark").forEach { choice ->
                    FilterChip(selected = theme == choice, onClick = { onTheme(choice) }, label = { Text(choice) }, modifier = Modifier.weight(1f))
                }
            }
            Text("${if (theme == "System") "Follows your device appearance." else "$theme appearance for the app and widgets."}",
                style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Eyebrow("Widget preview · ${today.year}")
            Surface(shape = RoundedCornerShape(24.dp), color = colors.surfaceVariant) { preview() }
        }
        Panel(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Background opacity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("${(opacity * 100).toInt()}%", style = MaterialTheme.typography.titleMedium)
            }
            Slider(modifier = Modifier.semantics { contentDescription = "Widget background opacity" }, value = opacity,
                onValueChange = onOpacity, onValueChangeFinished = onOpacityFinished, valueRange = 0f..1f)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Transparent", style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                Text("Solid", style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
            }
            Text("Applies to every widget. Text and dots stay fully visible.", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }
    }
}
