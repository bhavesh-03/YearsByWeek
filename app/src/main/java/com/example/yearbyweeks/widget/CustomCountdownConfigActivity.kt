package com.example.yearbyweeks.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.yearbyweeks.CountdownEditor
import com.example.yearbyweeks.data.AppPreferences
import com.example.yearbyweeks.data.Countdown
import com.example.yearbyweeks.ui.theme.YearByWeeksTheme
import kotlinx.coroutines.launch
import java.time.LocalDate

class CustomCountdownConfigActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)
        val widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) { finish(); return }
        val (name, date) = CustomCountdownWidget.getEvent(this, widgetId)
        setContent {
            YearByWeeksTheme(darkTheme = AppPreferences(this).isDark(this)) {
                Surface(Modifier.fillMaxSize()) {
                    CountdownEditor(Countdown(id = widgetId.toString(), name = name, date = date ?: LocalDate.now()), onDismiss = { finish() }, onSave = { event ->
                        CustomCountdownWidget.saveEvent(this, widgetId, event.name, event.date)
                        lifecycleScope.launch {
                            refreshWidgets(this@CustomCountdownConfigActivity)
                            setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId))
                            finish()
                        }
                    })
                }
            }
        }
    }
}
