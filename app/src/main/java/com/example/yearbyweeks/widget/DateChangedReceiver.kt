package com.example.yearbyweeks.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingWorkPolicy
import androidx.work.workDataOf

/** Recalculate local dates after date/time/zone or system appearance changes. */
class DateChangedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in setOf(Intent.ACTION_DATE_CHANGED, Intent.ACTION_TIME_CHANGED,
                Intent.ACTION_TIMEZONE_CHANGED, Intent.ACTION_CONFIGURATION_CHANGED)) return
        if (!hasWidgets(context)) return
        WorkManager.getInstance(context).enqueueUniqueWork(
            "widget_calendar_change", ExistingWorkPolicy.KEEP,
            OneTimeWorkRequestBuilder<WidgetRefreshWorker>()
                .setInputData(workDataOf("force_refresh" to true)).build()
        )
    }
}
