package com.k3h.yoke

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

fun airportLabel(code: String): String = AIRPORTS.find { it.code == code }?.label ?: code

/** Instant, offline suggestions for the typing box. */
fun searchAirports(query: String, limit: Int = 4): List<Airport> {
    val q = query.trim().lowercase()
    if (q.length < 2) return emptyList()
    return AIRPORTS
        .mapNotNull { airport ->
            val name = airport.name.lowercase()
            val code = airport.code.lowercase()
            val score = when {
                code == q -> 0
                name.startsWith(q) -> 1
                name.split(" ").any { it.startsWith(q) } -> 2
                name.contains(q) -> 3
                code.startsWith(q) -> 4
                else -> return@mapNotNull null
            }
            score to airport
        }
        .sortedBy { it.first }
        .take(limit)
        .map { it.second }
}

/**
 * "Aberdeen (ABZ)" -> ABZ, "abz" -> ABZ, "Aberdeen" -> ABZ (local list),
 * anything else -> asks Ignav's airport search. Returns null if nothing matches.
 */
suspend fun resolveAirportCode(input: String): String? {
    val text = input.trim()
    if (text.isEmpty()) return null

    Regex("\\(([A-Za-z]{3})\\)\\s*$").find(text)?.let { return it.groupValues[1].uppercase() }
    if (Regex("^[A-Za-z]{3}$").matches(text)) return text.uppercase()
    searchAirports(text, 1).firstOrNull()?.let { return it.code }
    return remoteAirportCode(text)
}

private suspend fun remoteAirportCode(query: String): String? = withContext(Dispatchers.IO) {
    val url = "https://ignav.com/api/airports?q=${URLEncoder.encode(query, "UTF-8")}&limit=1"
    val conn = URL(url).openConnection() as HttpURLConnection
    try {
        conn.connectTimeout = 15_000
        conn.readTimeout = 30_000
        conn.setRequestProperty("X-Api-Key", BuildConfig.IGNAV_API_KEY)
        if (conn.responseCode !in 200..299) return@withContext null
        val array = JSONArray(conn.inputStream.bufferedReader().use { it.readText() })
        if (array.length() == 0) null else array.getJSONObject(0).optString("code").ifBlank { null }
    } catch (e: Exception) {
        null
    } finally {
        conn.disconnect()
    }
}