package com.example.yearbyweeks.data

import android.content.Context
import android.content.res.Configuration
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.util.UUID

data class Countdown(val id: String = UUID.randomUUID().toString(), val name: String, val date: LocalDate)

class AppPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("appearance", Context.MODE_PRIVATE)
    var theme: String
        get() = prefs.getString("theme", "System") ?: "System"
        set(value) { prefs.edit().putString("theme", value).apply() }
    var opacity: Float
        get() = prefs.getFloat("opacity", 1f).coerceIn(0f, 1f)
        set(value) { prefs.edit().putFloat("opacity", value.coerceIn(0f, 1f)).apply() }
    fun isDark(context: Context): Boolean = when (theme) {
        "Dark" -> true
        "Light" -> false
        else -> context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }
    fun events(): List<Countdown> = runCatching {
        val array = JSONArray(prefs.getString("events", "[]"))
        (0 until array.length()).map { index ->
            val obj = array.getJSONObject(index)
            Countdown(obj.getString("id"), obj.getString("name"), LocalDate.parse(obj.getString("date")))
        }
    }.getOrDefault(emptyList())
    fun saveEvents(events: List<Countdown>) {
        val array = JSONArray()
        events.forEach { array.put(JSONObject().put("id", it.id).put("name", it.name).put("date", it.date.toString())) }
        prefs.edit().putString("events", array.toString()).apply()
    }
}
