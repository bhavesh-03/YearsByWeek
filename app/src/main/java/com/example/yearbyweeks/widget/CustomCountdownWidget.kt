package com.example.yearbyweeks.widget

import android.content.Context
import android.content.Intent
import android.appwidget.AppWidgetManager
import androidx.compose.ui.unit.dp
import androidx.glance.*
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.layout.*
import com.example.yearbyweeks.util.DateUtils
import java.time.LocalDate

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
            val date = dateStr?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: LocalDate.of(LocalDate.now().year + 1, 1, 1)
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
        val widgetId = GlanceAppWidgetManager(context).getAppWidgetId(id)
        val (name, date) = getEvent(context, widgetId)
        provideContent {
            val size = LocalSize.current
            val bitmap = WidgetRenderer.render(context, size.width.value, size.height.value,
                DateUtils.today(), eventName = name, eventDate = date)
            val editIntent = Intent(context, CustomCountdownConfigActivity::class.java)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            Box(GlanceModifier.fillMaxSize().appWidgetBackground().cornerRadius(22.dp)
                .clickable(actionStartActivity(editIntent))) {
                Image(ImageProvider(bitmap), "$name, $date, ${date?.let { DateUtils.daysUntil(it) }} days from today",
                    modifier = GlanceModifier.fillMaxSize(), contentScale = ContentScale.FillBounds)
            }
        }
    }
}
class CustomCountdownWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CustomCountdownWidget()
    override fun onEnabled(context: Context) { super.onEnabled(context); WidgetRefreshWorker.enqueue(context, newlyEnabled = true) }
    override fun onDisabled(context: Context) { super.onDisabled(context); WidgetRefreshWorker.enqueue(context) }
    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        appWidgetIds.forEach { CustomCountdownWidget.deleteEvent(context, it) }
        super.onDeleted(context, appWidgetIds)
    }
}
