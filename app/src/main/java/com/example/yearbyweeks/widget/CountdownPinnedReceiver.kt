package com.example.yearbyweeks.widget

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.time.LocalDate

class CountdownPinnedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        val name = intent.getStringExtra("event_name") ?: return
        val date = intent.getStringExtra("event_date")?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: return
        if (id == AppWidgetManager.INVALID_APPWIDGET_ID) return
        CustomCountdownWidget.saveEvent(context, id, name, date)
        WorkManager.getInstance(context).enqueue(OneTimeWorkRequestBuilder<WidgetRefreshWorker>().build())
    }
}
