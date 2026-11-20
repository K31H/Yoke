package com.k3h.yoke

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import androidx.core.content.edit

class Storage(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("yoke", Context.MODE_PRIVATE)

    fun loadSavedSearches(): List<SavedSearch> {
        val text = prefs.getString("saved_searches", null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(text)
            (0 until array.length()).map { i ->
                val o = array.getJSONObject(i)
                SavedSearch(
                    o.getString("departure"),
                    o.getString("destination"),
                    o.getString("departureDate"),
                    o.getString("returnDate")
                )
            }
        }.getOrDefault(emptyList())
    }

    fun saveSavedSearches(searches: List<SavedSearch>) {
        val array = JSONArray()
        searches.forEach {
            array.put(
                JSONObject()
                    .put("departure", it.departure)
                    .put("destination", it.destination)
                    .put("departureDate", it.departureDate)
                    .put("returnDate", it.returnDate)
            )
        }
        prefs.edit { putString("saved_searches", array.toString()) }
    }

    var currency: String
        get() = prefs.getString("currency", "GBP") ?: "GBP"
        set(value) = prefs.edit { putString("currency", value)}

    /** null until the user has picked a mode, so the first launch can follow the phone's setting. */
    var savedDarkMode: Boolean?
        get() = if (prefs.contains("dark_mode")) prefs.getBoolean("dark_mode", false) else null
        set(value) {
            prefs.edit {
                if (value == null) remove("dark_mode") else putBoolean("dark_mode", value)
            }
        }

    var homeAirport: String
        get() = prefs.getString("home_airport", "") ?: ""
        set(value) = prefs.edit { putString("home_airport", value)}
}