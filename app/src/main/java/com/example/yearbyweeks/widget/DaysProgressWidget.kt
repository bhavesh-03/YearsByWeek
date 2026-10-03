package com.example.yearbyweeks.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class DaysProgressWidget : ProgressWidget(false)
class DaysProgressWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DaysProgressWidget()
    override fun onEnabled(context: Context) { super.onEnabled(context); WidgetRefreshWorker.enqueue(context) }
}
