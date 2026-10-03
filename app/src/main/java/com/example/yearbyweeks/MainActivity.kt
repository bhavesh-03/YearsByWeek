package com.example.yearbyweeks

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.core.view.WindowCompat
import androidx.lifecycle.LifecycleEventObserver
import com.example.yearbyweeks.data.AppPreferences
import com.example.yearbyweeks.data.Countdown
import com.example.yearbyweeks.ui.theme.YearByWeeksTheme
import com.example.yearbyweeks.util.DateUtils
import com.example.yearbyweeks.widget.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WidgetRefreshWorker.enqueue(this)
        setContent { YearProgressApp() }
    }
}

@Composable
fun YearProgressApp() {
    val context = LocalContext.current
    val prefs = remember { AppPreferences(context) }
    var theme by remember { mutableStateOf(prefs.theme) }
    var opacity by remember { mutableFloatStateOf(prefs.opacity) }
    var events by remember { mutableStateOf(prefs.events()) }
    var today by remember { mutableStateOf(DateUtils.today()) }
    var showEditor by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<String?>(null) }
    val editing = events.firstOrNull { it.id == editingId }
    var deleting by remember { mutableStateOf<Countdown?>(null) }
    val scope = rememberCoroutineScope()
    val lifecycle = (context as ComponentActivity).lifecycle
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                today = DateUtils.today()
                if (hasWidgets(context) && prefs.lastWidgetRefreshDate != today) {
                    scope.launch { refreshWidgets(context) }
                }
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                today = DateUtils.today()
                delay(60_000L)
            }
        }
    }
    val dark = when (theme) { "Dark" -> true; "Light" -> false; else -> isSystemInDarkTheme() }
    SideEffect {
        WindowCompat.getInsetsController((context as ComponentActivity).window, context.window.decorView).apply {
            isAppearanceLightStatusBars = !dark
            isAppearanceLightNavigationBars = !dark
        }
    }
    YearByWeeksTheme(darkTheme = dark) {
        val colors = MaterialTheme.colorScheme
        var selectedTab by rememberSaveable { mutableIntStateOf(0) }
        Scaffold(
            containerColor = colors.background,
            topBar = {
                Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Image(painterResource(R.drawable.ic_launcher_foreground), "YearByWeeks logo",
                        Modifier.size(36.dp).background(androidx.compose.ui.graphics.Color(0xFF090909), RoundedCornerShape(11.dp)))
                    Text("YearByWeeks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f))
                    Text("${today.year}", style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant,
                        letterSpacing = 1.sp)
                }
            },
            bottomBar = {
                Column {
                    HorizontalDivider(color = colors.outlineVariant)
                    NavigationBar(containerColor = colors.background, tonalElevation = 0.dp) {
                        listOf("Year", "Countdowns", "Style").forEachIndexed { index, label ->
                            NavigationBarItem(selected = selectedTab == index, onClick = { selectedTab = index },
                                icon = { Icon(painterResource(listOf(R.drawable.ic_overview, R.drawable.ic_countdowns, R.drawable.ic_appearance)[index]), null, Modifier.size(21.dp)) },
                                label = { Text(label) },
                                colors = NavigationBarItemDefaults.colors(indicatorColor = colors.surfaceVariant,
                                    selectedIconColor = colors.onSurface, selectedTextColor = colors.onSurface,
                                    unselectedIconColor = colors.onSurfaceVariant, unselectedTextColor = colors.onSurfaceVariant))
                        }
                    }
                }
            }
        ) { insets ->
            Box(Modifier.fillMaxSize().padding(insets).consumeWindowInsets(insets)) {
                when (selectedTab) {
                    0 -> YearDashboard(today, onPin = { weeks ->
                        pinWidget(context, if (weeks) YearProgressWidgetReceiver::class.java else DaysProgressWidgetReceiver::class.java)
                    })
                    1 -> CountdownDashboard(today, events,
                        onNew = { editingId = null; showEditor = true },
                        onEdit = { editingId = it.id; showEditor = true },
                        onDelete = { deleting = it },
                        onPin = { pinWidget(context, CustomCountdownWidgetReceiver::class.java, it) })
                    2 -> AppearanceDashboard(today, theme, opacity,
                        onTheme = { theme = it; prefs.theme = it; scope.launch { refreshWidgets(context) } },
                        onOpacity = { opacity = it },
                        onOpacityFinished = { prefs.opacity = opacity; scope.launch { refreshWidgets(context) } },
                        preview = { WidgetPreview(today, opacity = opacity, theme = theme, dark = dark) })
                }
            }
        }
        if (showEditor) CountdownEditor(initial = editing, onDismiss = { showEditor = false }, onSave = { event ->
            events = if (editing == null) events + event else events.map { if (it.id == event.id) event else it }
            prefs.saveEvents(events); showEditor = false
        })
        deleting?.let { event ->
            AlertDialog(onDismissRequest = { deleting = null }, title = { Text("Delete ${event.name}?") },
                text = { Text("This removes the countdown from the app. Home-screen widgets can be removed separately.") },
                confirmButton = { TextButton(onClick = { events = events.filterNot { it.id == event.id }; prefs.saveEvents(events); deleting = null }) { Text("Delete") } },
                dismissButton = { TextButton(onClick = { deleting = null }) { Text("Keep") } })
        }
    }
}

@Composable
private fun WidgetPreview(today: LocalDate, weeks: Boolean = false, event: Countdown? = null, opacity: Float, theme: String, dark: Boolean) {
    val context = LocalContext.current
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val width = maxWidth.value
        val height = if (event != null) 112f else if (weeks) 170f else 190f
        val bitmap = remember(today, weeks, event, opacity, theme, dark, width, context.resources.displayMetrics.density) {
            WidgetRenderer.render(context, width, height, today, weeks, event?.name, event?.date,
                opacityOverride = opacity)
        }
        Image(bitmap.asImageBitmap(), if (event == null) "${DateUtils.remainingDays(today)} days left, including today" else "${event.name}, ${DateUtils.daysUntil(event.date, today)} days from today",
            Modifier.fillMaxWidth().height(height.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountdownEditor(initial: Countdown?, onDismiss: () -> Unit, onSave: (Countdown) -> Unit) {
    var name by rememberSaveable(initial?.id) { mutableStateOf(initial?.name ?: "") }
    var dateText by rememberSaveable(initial?.id) { mutableStateOf((initial?.date ?: DateUtils.today().plusDays(30)).toString()) }
    var picker by rememberSaveable { mutableStateOf(false) }
    val date = LocalDate.parse(dateText)
    AlertDialog(onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "New countdown" else "Edit countdown") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(name, { name = it.take(60) }, label = { Text("Event name") }, placeholder = { Text("e.g. Summer trip") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedButton(onClick = { picker = true }, modifier = Modifier.fillMaxWidth()) { Text(date.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))) }
                Text("Today is 0, tomorrow is 1. Past events show days since.", style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = { TextButton(enabled = name.isNotBlank(), onClick = { onSave(Countdown(initial?.id ?: java.util.UUID.randomUUID().toString(), name.trim(), date)) }) { Text("Save countdown") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
    if (picker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = date.toEpochDay() * 86_400_000L)
        DatePickerDialog(onDismissRequest = { picker = false }, confirmButton = {
            TextButton(enabled = state.selectedDateMillis != null, onClick = {
                state.selectedDateMillis?.let { dateText = LocalDate.ofEpochDay(Math.floorDiv(it, 86_400_000L)).toString() }; picker = false
            }) { Text("Choose date") }
        }, dismissButton = { TextButton(onClick = { picker = false }) { Text("Cancel") } }) { DatePicker(state) }
    }
}

private fun pinWidget(context: Context, receiver: Class<*>, event: Countdown? = null) {
    val manager = AppWidgetManager.getInstance(context)
    if (!manager.isRequestPinAppWidgetSupported) {
        Toast.makeText(context, "Touch and hold your home screen, then choose Widgets → YearByWeeks.", Toast.LENGTH_LONG).show()
        return
    }
    val callback = event?.let {
        val intent = Intent(context, CountdownPinnedReceiver::class.java)
            .putExtra("event_name", it.name).putExtra("event_date", it.date.toString())
        PendingIntent.getBroadcast(context, it.id.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
    }
    manager.requestPinAppWidget(ComponentName(context, receiver), null, callback)
}
