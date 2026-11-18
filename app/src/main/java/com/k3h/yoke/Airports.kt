package com.k3h.yoke

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat

data class Airport(
    val code: String,
    val name: String,
    val lat: Double,
    val lon: Double
) {
    val label: String get() = "$name ($code)"
}

data class AirportMatch(val airport: Airport, val distanceKm: Float)

fun nearestAirport(lat: Double, lon: Double): AirportMatch {
    val results = FloatArray(1)
    var best: AirportMatch? = null
    for (airport in AIRPORTS) {
        Location.distanceBetween(lat, lon, airport.lat, airport.lon, results)
        val km = results[0] / 1000f
        if (best == null || km < best.distanceKm) best = AirportMatch(airport, km)
    }
    return best!!
}

/**
 * Gets a location without any extra dependencies: uses a recent cached fix if one exists,
 * otherwise asks for a fresh one (Android 11+). Calls onResult(null) if it can't.
 */
@SuppressLint("MissingPermission")
fun requestLocation(context: Context, onResult: (Location?) -> Unit) {
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val providers = lm.getProviders(true).filter { it != LocationManager.PASSIVE_PROVIDER }
    if (providers.isEmpty()) {
        onResult(null)   // location is switched off on the device
        return
    }

    val cached = providers
        .mapNotNull { runCatching { lm.getLastKnownLocation(it) }.getOrNull() }
        .maxByOrNull { it.time }
    if (cached != null) {
        onResult(cached)
        return
    }

    if (Build.VERSION.SDK_INT >= 30) {
        runCatching {
            lm.getCurrentLocation(providers.first(), null, ContextCompat.getMainExecutor(context)) {
                onResult(it)
            }
        }.onFailure { onResult(null) }
    } else {
        onResult(null)
    }
}

// A built-in set of major airports (name, IATA code, latitude, longitude).
val AIRPORTS = listOf(
    // UK & Ireland
    Airport("ABZ", "Aberdeen", 57.2019, -2.1978),
    Airport("EDI", "Edinburgh", 55.9500, -3.3725),
    Airport("GLA", "Glasgow", 55.8719, -4.4331),
    Airport("PIK", "Glasgow Prestwick", 55.5094, -4.5867),
    Airport("INV", "Inverness", 57.5425, -4.0475),
    Airport("NCL", "Newcastle", 55.0375, -1.6917),
    Airport("MME", "Teesside", 54.5092, -1.4294),
    Airport("MAN", "Manchester", 53.3537, -2.2750),
    Airport("LPL", "Liverpool", 53.3336, -2.8497),
    Airport("LBA", "Leeds Bradford", 53.8659, -1.6606),
    Airport("EMA", "East Midlands", 52.8311, -1.3281),
    Airport("BHX", "Birmingham", 52.4539, -1.7480),
    Airport("NWI", "Norwich", 52.6758, 1.2828),
    Airport("BRS", "Bristol", 51.3827, -2.7191),
    Airport("CWL", "Cardiff", 51.3967, -3.3433),
    Airport("EXT", "Exeter", 50.7344, -3.4139),
    Airport("SOU", "Southampton", 50.9503, -1.3568),
    Airport("LHR", "London Heathrow", 51.4700, -0.4543),
    Airport("LGW", "London Gatwick", 51.1537, -0.1821),
    Airport("STN", "London Stansted", 51.8860, 0.2389),
    Airport("LTN", "London Luton", 51.8747, -0.3683),
    Airport("LCY", "London City", 51.5053, 0.0553),
    Airport("BFS", "Belfast International", 54.6575, -6.2158),
    Airport("BHD", "Belfast City", 54.6181, -5.8725),
    Airport("DUB", "Dublin", 53.4213, -6.2701),
    Airport("ORK", "Cork", 51.8413, -8.4911),
    Airport("SNN", "Shannon", 52.7020, -8.9248),

    // Europe
    Airport("CDG", "Paris", 49.0097, 2.5479),
    Airport("AMS", "Amsterdam", 52.3105, 4.7683),
    Airport("BRU", "Brussels", 50.9010, 4.4844),
    Airport("FRA", "Frankfurt", 50.0379, 8.5622),
    Airport("MUC", "Munich", 48.3538, 11.7861),
    Airport("ZRH", "Zurich", 47.4647, 8.5492),
    Airport("VIE", "Vienna", 48.1103, 16.5697),
    Airport("MAD", "Madrid", 40.4983, -3.5676),
    Airport("BCN", "Barcelona", 41.2974, 2.0833),
    Airport("LIS", "Lisbon", 38.7742, -9.1342),
    Airport("FCO", "Rome", 41.8003, 12.2389),
    Airport("ATH", "Athens", 37.9364, 23.9445),
    Airport("IST", "Istanbul", 41.2753, 28.7519),
    Airport("CPH", "Copenhagen", 55.6180, 12.6508),
    Airport("OSL", "Oslo", 60.1976, 11.1004),
    Airport("ARN", "Stockholm", 59.6498, 17.9238),

    // North America
    Airport("JFK", "New York", 40.6413, -73.7781),
    Airport("EWR", "New York Newark", 40.6895, -74.1745),
    Airport("BOS", "Boston", 42.3656, -71.0096),
    Airport("IAD", "Washington Dulles", 38.9531, -77.4565),
    Airport("ATL", "Atlanta", 33.6407, -84.4277),
    Airport("MIA", "Miami", 25.7959, -80.2870),
    Airport("ORD", "Chicago", 41.9742, -87.9073),
    Airport("DFW", "Dallas Fort Worth", 32.8998, -97.0403),
    Airport("LAX", "Los Angeles", 33.9416, -118.4085),
    Airport("SFO", "San Francisco", 37.6213, -122.3790),
    Airport("SEA", "Seattle", 47.4502, -122.3088),
    Airport("YYZ", "Toronto", 43.6777, -79.6248),

    // Middle East, Africa, Asia, Oceania
    Airport("DXB", "Dubai", 25.2532, 55.3657),
    Airport("DOH", "Doha", 25.2731, 51.6081),
    Airport("CAI", "Cairo", 30.1219, 31.4056),
    Airport("LOS", "Lagos", 6.5774, 3.3212),
    Airport("ABV", "Abuja", 9.0068, 7.2632),
    Airport("ACC", "Accra", 5.6052, -0.1668),
    Airport("NBO", "Nairobi", -1.3192, 36.9278),
    Airport("JNB", "Johannesburg", -26.1337, 28.2420),
    Airport("DEL", "Delhi", 28.5562, 77.1000),
    Airport("BOM", "Mumbai", 19.0896, 72.8656),
    Airport("SIN", "Singapore", 1.3644, 103.9915),
    Airport("HKG", "Hong Kong", 22.3080, 113.9185),
    Airport("HND", "Tokyo Haneda", 35.5494, 139.7798),
    Airport("NRT", "Tokyo Narita", 35.7720, 140.3929),
    Airport("SYD", "Sydney", -33.9399, 151.1753),
    Airport("MEL", "Melbourne", -37.6690, 144.8410)
)