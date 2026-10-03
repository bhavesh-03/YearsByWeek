package com.example.yearbyweeks.widget

import android.content.Context
import android.content.Intent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import androidx.compose.ui.unit.dp
import androidx.glance.*
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.layout.*
import androidx.work.*
import com.example.yearbyweeks.MainActivity
import com.example.yearbyweeks.data.AppPreferences
import com.example.yearbyweeks.util.DateUtils
import java.util.concurrent.TimeUnit

open class ProgressWidget(private val weeks: Boolean) : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val size = LocalSize.current
            val date = DateUtils.today()
            val bitmap = WidgetRenderer.render(context, size.width.value, size.height.value, date, weeks)
            Box(GlanceModifier.fillMaxSize().appWidgetBackground().cornerRadius(22.dp)
                .clickable(actionStartActivity(Intent(context, MainActivity::class.java)))) {
                Image(ImageProvider(bitmap), "${DateUtils.remainingDays(date)} days remaining in ${date.year}, including today",
                    modifier = GlanceModifier.fillMaxSize(), contentScale = ContentScale.FillBounds)
            }
        }
    }
}
class YearProgressWidget : ProgressWidget(true)
class YearProgressWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = YearProgressWidget()
    override fun onEnabled(context: Context) { super.onEnabled(context); WidgetRefreshWorker.enqueue(context, newlyEnabled = true) }
    override fun onDisabled(context: Context) { super.onDisabled(context); WidgetRefreshWorker.enqueue(context) }
}

fun hasWidgets(context: Context): Boolean {
    val manager = AppWidgetManager.getInstance(context)
    return listOf(
        YearProgressWidgetReceiver::class.java,
        DaysProgressWidgetReceiver::class.java,
        CustomCountdownWidgetReceiver::class.java
    ).any { manager.getAppWidgetIds(ComponentName(context, it)).isNotEmpty() }
}

suspend fun refreshWidgets(context: Context) {
    if (!hasWidgets(context)) return
    val refreshedDate = DateUtils.today()
    YearProgressWidget().updateAll(context)
    DaysProgressWidget().updateAll(context)
    CustomCountdownWidget().updateAll(context)
    AppPreferences(context).lastWidgetRefreshDate = refreshedDate
}

class WidgetRefreshWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = try {
        if (inputData.getBoolean("force_refresh", false) ||
            AppPreferences(applicationContext).lastWidgetRefreshDate != DateUtils.today()) {
            refreshWidgets(applicationContext)
        }
        Result.success()
    } catch (e: Exception) {
        if (e is kotlinx.coroutines.CancellationException) throw e
        Result.retry()
    }
    companion object {
        private const val WORK_NAME = "year_progress_calendar_refresh"
        private val OLD_WORK_NAMES = listOf("year_progress_widget_refresh", "year_progress_daily_refresh")

        fun enqueue(context: Context, newlyEnabled: Boolean = false) {
            val workManager = WorkManager.getInstance(context)
            val prefs = context.getSharedPreferences("widget_refresh_schedule", Context.MODE_PRIVATE)
            if (!prefs.getBoolean("migrated_to_calendar_refresh", false)) {
                OLD_WORK_NAMES.forEach(workManager::cancelUniqueWork)
                prefs.edit().putBoolean("migrated_to_calendar_refresh", true).apply()
            }
            if (newlyEnabled || hasWidgets(context)) {
                workManager.enqueueUniquePeriodicWork(
                    WORK_NAME, ExistingPeriodicWorkPolicy.KEEP,
                    PeriodicWorkRequestBuilder<WidgetRefreshWorker>(30, TimeUnit.MINUTES).build()
                )
            } else {
                workManager.cancelUniqueWork(WORK_NAME)
            }
        }
    }
}
