package com.k3h.yoke

import android.util.Log
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

        when (code) {
            in 200..299 -> JSONObject(text)
            401, 403 -> throw ApiException("The API key was rejected. Check IGNAV_API_KEY.")
            404 -> throw ApiException("Nothing found.")
            else -> throw ApiException(
                "Search failed (HTTP $code)." + if (BuildConfig.DEBUG) " ${text.take(120)}" else ""
            )
        }
    } catch (e: java.io.IOException) {
        throw ApiException("Couldn't reach the server. Check your internet connection.")
    } finally {
        conn.disconnect()
    }
}

// ---------- Public calls ----------
/** Nonstop and one-connection flights. Pass returnDate for a round trip (price = whole trip). */
suspend fun searchConnections(
    origin: String,
    destination: String,
    departDate: String,          // YYYY-MM-DD
    returnDate: String?,         // YYYY-MM-DD or null
    market: String // 2-letter country code; decides the currency
): List<FlightResult> {
    val roundTrip = returnDate != null

    val body = JSONObject()
        .put("origin", origin)
        .put("destination", destination)
        .put("departure_date", departDate)
        .put("max_stops", 1)
        .put("market", market)

    if (roundTrip) body.put("return_date", returnDate)

    val json = post(if (roundTrip) "/fares/round-trip" else "/fares/one-way", body)
    val itineraries = json.optJSONArray("itineraries") ?: JSONArray()

    if (BuildConfig.DEBUG) {
        Log.d("Yoke", "Search $origin -> $destination on $departDate (market $market): ${itineraries.length()} itineraries")
    }

    // Same outbound flights can come back at different prices: keep only the cheapest.
    val cheapest = LinkedHashMap<String, Pair<Double, FlightResult>>()

    for (i in 0 until itineraries.length()) {
        val itin = itineraries.getJSONObject(i)
        val outboundLeg = itin.optJSONObject("outbound") ?: continue
        val priceObj = itin.getJSONObject("price")
        val amount = priceObj.getDouble("amount")

        val key = legKey(outboundLeg)
        val existing = cheapest[key]

        if (existing != null && existing.first <= amount) continue

        val priceText = formatPrice(priceObj) + if (roundTrip) " return" else ""
        val outbound = buildLeg(outboundLeg, priceText, itin.optString("ignav_id")) ?: continue

        val inboundLeg = itin.optJSONObject("inbound")

        val inbound = if (roundTrip && inboundLeg != null) {
            buildLeg(inboundLeg, "", "") ?: continue
        } else null

        cheapest[key] = amount to outbound.copy(inbound = inbound)
    }

    if (cheapest.isEmpty()) {
        throw ApiException(
            if (itineraries.length() == 0)
                "No flights found from $origin to $destination on $departDate. Try different dates."
            else
                "Found ${itineraries.length()} flights from $origin to $destination, but all had more than one stop. Try different dates."
        )
    }
    return cheapest.values.sortedBy { it.first }.map { it.second }
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

// ---------- Building results ----------

/** One direction of travel (nonstop or one connection). Returns null for anything longer. */
internal fun buildLeg(leg: JSONObject, price: String, ignavId: String): FlightResult? {
    val segments = leg.optJSONArray("segments") ?: return null
    if (segments.length() !in 1..2) return null

    val s1 = segments.getJSONObject(0)
    val s2 = if (segments.length() == 2) segments.getJSONObject(1) else null
    val last = s2 ?: s1

    return FlightResult(
        fromCode = s1.getString("departure_airport"),
        fromTime = timeOf(s1.getString("departure_time_local")),
        fromDate = dateOf(s1.getString("departure_time_local")),

        leg1Duration = formatMinutes(s1.getInt("duration_minutes")),
        layoverCode = if (s2 != null) s1.getString("arrival_airport") else "",
        layoverDuration = if (s2 != null)
            formatMinutes(minutesBetween(s1.getString("arrival_time_local"), s2.getString("departure_time_local")))
        else "",
        leg2Duration = if (s2 != null) formatMinutes(s2.getInt("duration_minutes")) else "",

        toCode = last.getString("arrival_airport"),
        toTime = timeOf(last.getString("arrival_time_local")),
        toDate = dateOf(last.getString("arrival_time_local")),

        airline = leg.optString("carrier").ifBlank { s1.optString("operating_carrier_name", "") },

        airlineCode = s1.optString("marketing_carrier_code"),
        price = price,

        layoverArrivalTime = if (s2 != null) timeOf(s1.getString("arrival_time_local")) else "",
        layoverDepartureTime = if (s2 != null) timeOf(s2.getString("departure_time_local")) else "",

        provider = "",
        ignavId = ignavId
    )
}

/** Identifies a leg by its flight numbers and departure times, for spotting duplicates. */
internal fun legKey(leg: JSONObject): String {
    val segments = leg.optJSONArray("segments") ?: return ""
    return (0 until segments.length()).joinToString("|") { n ->
        val s = segments.getJSONObject(n)
        "${s.optString("marketing_carrier_code")}${s.optString("flight_number")}@${s.optString("departure_time_local")}"
    }
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

internal fun formatMinutes(total: Int): String {
    val h = total / 60
    val m = total % 60
    return when {
        h == 0 -> "${m}m"
        m == 0 -> "${h}h"
        else -> "${h}h ${m}m"
    }
}

internal fun formatPrice(price: JSONObject): String {
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