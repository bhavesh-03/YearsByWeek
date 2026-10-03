package com.example.yearbyweeks.widget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.unit.dp
import androidx.glance.*
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.layout.*
import androidx.work.*
import com.example.yearbyweeks.MainActivity
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
    override fun onEnabled(context: Context) { super.onEnabled(context); WidgetRefreshWorker.enqueue(context) }
}

suspend fun refreshWidgets(context: Context) {
    YearProgressWidget().updateAll(context)
    DaysProgressWidget().updateAll(context)
    CustomCountdownWidget().updateAll(context)
}

class WidgetRefreshWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = try {
        refreshWidgets(applicationContext)
        Result.success()
    } catch (e: Exception) {
        if (e is kotlinx.coroutines.CancellationException) throw e
        Result.retry()
    }
    companion object {
        fun enqueue(context: Context) {
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "year_progress_widget_refresh", ExistingPeriodicWorkPolicy.UPDATE,
                PeriodicWorkRequestBuilder<WidgetRefreshWorker>(30, TimeUnit.MINUTES).build()
            )
        }
    }
}
