package com.k3h.yoke

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt

private const val BASE_URL = "https://ignav.com/api"

class ApiException(message: String) : Exception(message)

data class BookingOption(val provider: String, val price: String, val url: String)

// ---------- Networking ----------

private suspend fun post(path: String, body: JSONObject): JSONObject = withContext(Dispatchers.IO) {
    val conn = URL("$BASE_URL$path").openConnection() as HttpURLConnection
    try {
        conn.requestMethod = "POST"
        conn.connectTimeout = 15_000
        conn.readTimeout = 60_000            // fare searches can take a while
        conn.setRequestProperty("X-Api-Key", BuildConfig.IGNAV_API_KEY)
        conn.setRequestProperty("Content-Type", "application/json")
        conn.doOutput = true
        conn.outputStream.use { it.write(body.toString().toByteArray()) }

        val code = conn.responseCode
        val text = (if (code in 200..299) conn.inputStream else conn.errorStream)
            ?.bufferedReader()?.use { it.readText() }.orEmpty()

        when {
            code in 200..299 -> JSONObject(text)
            code == 401 || code == 403 -> throw ApiException("The API key was rejected. Check IGNAV_API_KEY.")
            code == 404 -> throw ApiException("Nothing found.")
            else -> throw ApiException("Search failed (HTTP $code). ${text.take(120)}")
        }
    } catch (e: java.io.IOException) {
        throw ApiException("Couldn't reach the server. Check your internet connection.")
    } finally {
        conn.disconnect()
    }
}

// ---------- Public calls ----------

/** Connecting flights (exactly one stop). Pass returnDate for a round trip (price = whole trip). */
suspend fun searchConnections(
    origin: String,
    destination: String,
    departDate: String,          // YYYY-MM-DD
    returnDate: String?          // YYYY-MM-DD or null
): List<FlightResult> {
    val roundTrip = returnDate != null
    val body = JSONObject()
        .put("origin", origin)
        .put("destination", destination)
        .put("departure_date", departDate)
        .put("max_stops", 1)
        .put("market", "GB")                       // prices in GBP
    if (roundTrip) body.put("return_date", returnDate)

    val json = post(if (roundTrip) "/fares/round-trip" else "/fares/one-way", body)
    val itineraries = json.optJSONArray("itineraries") ?: JSONArray()

    val found = mutableListOf<Pair<Double, FlightResult>>()
    for (i in 0 until itineraries.length()) {
        val itin = itineraries.getJSONObject(i)
        val outbound = itin.optJSONObject("outbound") ?: continue
        val segments = outbound.optJSONArray("segments") ?: continue
        if (segments.length() != 2) continue       // Yoke shows one-connection trips only

        val s1 = segments.getJSONObject(0)
        val s2 = segments.getJSONObject(1)
        val price = itin.getJSONObject("price")
        val amount = price.getDouble("amount")

        val layoverMinutes = minutesBetween(
            s1.getString("arrival_time_local"),
            s2.getString("departure_time_local")
        )

        found += amount to FlightResult(
            fromCode = s1.getString("departure_airport"),
            fromTime = timeOf(s1.getString("departure_time_local")),
            fromDate = dateOf(s1.getString("departure_time_local")),
            leg1Duration = formatMinutes(s1.getInt("duration_minutes")),
            layoverCode = s1.getString("arrival_airport"),
            layoverDuration = formatMinutes(layoverMinutes),
            leg2Duration = formatMinutes(s2.getInt("duration_minutes")),
            toCode = s2.getString("arrival_airport"),
            toTime = timeOf(s2.getString("arrival_time_local")),
            toDate = dateOf(s2.getString("arrival_time_local")),
            airline = outbound.optString("carrier").ifBlank { s1.optString("operating_carrier_name", "") },
            price = formatPrice(price) + if (roundTrip) " return" else "",
            layoverArrivalTime = timeOf(s1.getString("arrival_time_local")),
            layoverDepartureTime = timeOf(s2.getString("departure_time_local")),
            provider = "",
            ignavId = itin.optString("ignav_id")
        )
    }
    return found.sortedBy { it.first }.map { it.second }
}

/** Where to buy a specific itinerary (airlines and online travel agents). */
suspend fun fetchBookingOptions(ignavId: String): List<BookingOption> {
    val json = post("/fares/booking-links", JSONObject().put("ignav_id", ignavId))
    val options = json.optJSONArray("booking_options") ?: return emptyList()

    // Round trips can come back as separate outbound/inbound options; prefer ones covering the most legs.
    val maxLegs = (0 until options.length()).maxOfOrNull {
        options.getJSONObject(it).optJSONArray("legs")?.length() ?: 0
    } ?: 0

    val result = mutableListOf<BookingOption>()
    for (i in 0 until options.length()) {
        val option = options.getJSONObject(i)
        if ((option.optJSONArray("legs")?.length() ?: 0) != maxLegs) continue
        val links = option.optJSONArray("links") ?: continue
        for (j in 0 until links.length()) {
            val link = links.getJSONObject(j)
            var url = link.optString("url")
            if (url.isBlank()) continue
            if (!url.startsWith("http")) url = "https://$url"
            result += BookingOption(
                provider = link.optString("provider_name", "Unknown"),
                price = link.optJSONObject("price")?.let { formatPrice(it) } ?: "",
                url = url
            )
        }
    }
    return result
}

// ---------- Helpers ----------

/** Accepts 2026-12-16 or 16/12/26 or 16/12/2026. Returns YYYY-MM-DD, or null if invalid. */
fun normaliseDate(input: String): String? {
    for (pattern in listOf("yyyy-MM-dd", "d/M/yy")) {
        val parser = SimpleDateFormat(pattern, Locale.UK).apply { isLenient = false }
        val date = runCatching { parser.parse(input.trim()) }.getOrNull() ?: continue
        return SimpleDateFormat("yyyy-MM-dd", Locale.UK).format(date)
    }
    return null
}

private fun minutesBetween(from: String, to: String): Int {
    val f = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.UK)
    return ((f.parse(to)!!.time - f.parse(from)!!.time) / 60_000).toInt()
}

private fun timeOf(local: String) = local.substring(11, 16)                     // "HH:mm"
private fun dateOf(local: String) =
    "${local.substring(8, 10)}/${local.substring(5, 7)}/${local.substring(2, 4)}"  // "dd/MM/yy"

private fun formatMinutes(total: Int): String {
    val h = total / 60
    val m = total % 60
    val hours = if (h == 1) "1 hour" else "$h hours"
    return when {
        h == 0 -> "$m minutes"
        m == 0 -> hours
        else -> "$hours $m minutes"
    }
}

private fun formatPrice(price: JSONObject): String {
    val amount = price.optDouble("amount", 0.0)
    if (amount == 0.0) return ""
    val symbol = when (val c = price.optString("currency")) {
        "GBP" -> "£"
        "USD" -> "$"
        "EUR" -> "€"
        else -> "$c "
    }
    return "$symbol${amount.roundToInt()}"
}