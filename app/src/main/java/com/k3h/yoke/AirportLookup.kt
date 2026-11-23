package com.k3h.yoke

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/** One row in the suggestion list. */
data class AirportSuggestion(val code: String, val label: String, val detail: String = "")

/** Names learned from airport searches this session, so codes can be shown as names. */
object AirportNames {
    val labels = mutableMapOf<String, String>()
}

fun airportLabel(code: String): String =
    AIRPORTS.find { it.code == code }?.label ?: AirportNames.labels[code] ?: code

/** Instant, offline matches from the built-in list. */
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

private val searchCache = mutableMapOf<String, List<AirportSuggestion>>()

/** Asks Ignav's airport search (worldwide). Results are cached per query. */
suspend fun fetchAirportSuggestions(query: String): List<AirportSuggestion> {
    val key = query.trim().lowercase()

    if (key.length < 2) return emptyList()
    searchCache[key]?.let { return it }

    val result: List<AirportSuggestion> = withContext(Dispatchers.IO) {
        val url = "https://ignav.com/api/airports?q=${URLEncoder.encode(query.trim(), "UTF-8")}&limit=5"
        val conn = URL(url).openConnection() as HttpURLConnection

        try {
            conn.connectTimeout = 15_000
            conn.readTimeout = 30_000
            conn.setRequestProperty("X-Api-Key", BuildConfig.IGNAV_API_KEY)

            if (conn.responseCode !in 200..299) return@withContext null
            val array = JSONArray(conn.inputStream.bufferedReader().use { it.readText() })
            (0 until array.length()).mapNotNull { i ->
                val o = array.getJSONObject(i)
                val code = o.optString("code")

                if (code.isBlank()) return@mapNotNull null
                val name = o.optString("name")
                val city = o.optString("city").ifBlank { name }

                AirportSuggestion(
                    code = code,
                    label = "$city ($code)",
                    detail = listOf(name, o.optString("country")).filter { it.isNotBlank() }.joinToString(", ")
                )
            }
        } catch (_: Exception) {
            null
        } finally {
            conn.disconnect()
        }
    } ?: return emptyList()

    searchCache[key] = result
    result.forEach { AirportNames.labels[it.code] = it.label }
    return result
}

/**
 * "Orlando (MCO)" -> MCO, "abz" -> ABZ, "Aberdeen" -> ABZ (built-in list),
 * anything else -> first match from Ignav's airport search. Null if nothing matches.
 */
suspend fun resolveAirportCode(input: String): String? {
    val text = input.trim()
    if (text.isEmpty()) return null

    Regex("\\(([A-Za-z]{3})\\)\\s*$").find(text)?.let { return it.groupValues[1].uppercase() }
    if (Regex("^[A-Za-z]{3}$").matches(text)) return text.uppercase()
    searchAirports(text, 1).firstOrNull()?.let { return it.code }
    return fetchAirportSuggestions(text).firstOrNull()?.code
}