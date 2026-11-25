package com.k3h.yoke

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FlightParsingTest {

    // builds one flight segment the way the API sends it
    private fun segment(
        flightNumber: String,
        from: String,
        departs: String,
        to: String,
        arrives: String,
        minutes: Int
    ) = """
        {
          "marketing_carrier_code": "BA",
          "flight_number": "$flightNumber",
          "departure_airport": "$from",
          "departure_time_local": "$departs",
          "arrival_airport": "$to",
          "arrival_time_local": "$arrives",
          "duration_minutes": $minutes
        }
    """.trimIndent()

    // builds one direction of a trip out of segments
    private fun leg(vararg segments: String) =
        JSONObject("""{"carrier": "British Airways", "segments": [${segments.joinToString(",")}]}""")

    private val toLondon = segment("1301", "ABZ", "2026-12-16T08:00:00", "LHR", "2026-12-16T09:30:00", 90)
    private val toNewYork = segment("175", "LHR", "2026-12-16T11:30:00", "JFK", "2026-12-16T14:20:00", 470)

    // ---------- building a flight from the API's data ----------

    @Test
    fun oneStopFlightIsReadCorrectly() {

        val result = buildLeg(leg(toLondon, toNewYork), "£400", "abc123")!!

        assertEquals("ABZ", result.fromCode)
        assertEquals("08:00", result.fromTime)
        assertEquals("16/12/26", result.fromDate)
        assertEquals("1h 30m", result.leg1Duration)

        assertEquals("LHR", result.layoverCode)
        assertEquals("09:30", result.layoverArrivalTime)
        assertEquals("11:30", result.layoverDepartureTime)
        assertEquals("2h", result.layoverDuration)

        assertEquals("7h 50m", result.leg2Duration)
        assertEquals("JFK", result.toCode)
        assertEquals("14:20", result.toTime)

        assertEquals("British Airways", result.airline)
        assertEquals("BA", result.airlineCode)
        assertEquals("£400", result.price)
        assertEquals("abc123", result.ignavId)
        assertFalse(result.isNonstop)

    }

    @Test
    fun nonstopFlightHasNoLayover() {

        val result = buildLeg(leg(toLondon), "£90", "xyz")!!

        assertTrue(result.isNonstop)
        assertEquals("", result.layoverCode)
        assertEquals("", result.layoverDuration)
        assertEquals("", result.leg2Duration)
        assertEquals("LHR", result.toCode)
        assertEquals("09:30", result.toTime)

    }

    @Test
    fun flightWithMoreThanOneStopIsRejected() {

        val extra = segment("99", "JFK", "2026-12-16T16:00:00", "MCO", "2026-12-16T18:30:00", 150)

        assertNull(buildLeg(leg(toLondon, toNewYork, extra), "£300", "id"))

    }

    @Test
    fun flightWithNoSegmentsIsRejected() {

        assertNull(buildLeg(JSONObject("""{"carrier": "British Airways"}"""), "£1", "id"))
        assertNull(buildLeg(leg(), "£1", "id"))

    }

    @Test
    fun layoverThatCrossesMidnightIsCalculatedCorrectly() {

        val first = segment("1", "ABZ", "2026-12-16T21:00:00", "LHR", "2026-12-16T23:30:00", 150)
        val second = segment("2", "LHR", "2026-12-17T01:00:00", "JFK", "2026-12-17T04:00:00", 480)

        val result = buildLeg(leg(first, second), "£500", "id")!!

        assertEquals("1h 30m", result.layoverDuration)
        assertEquals("16/12/26", result.fromDate)
        assertEquals("17/12/26", result.toDate)

    }

    // ---------- formatting durations and prices ----------

    @Test
    fun durationsAreShownAsHoursAndMinutes() {

        assertEquals("1h 30m", formatMinutes(90))
        assertEquals("2h", formatMinutes(120))
        assertEquals("45m", formatMinutes(45))
        assertEquals("0m", formatMinutes(0))
        assertEquals("10h 5m", formatMinutes(605))

    }

    @Test
    fun pricesUseTheRightCurrencySymbol() {

        assertEquals("£400", formatPrice(JSONObject("""{"amount": 400, "currency": "GBP"}""")))
        assertEquals("\$400", formatPrice(JSONObject("""{"amount": 400, "currency": "USD"}""")))
        assertEquals("€400", formatPrice(JSONObject("""{"amount": 400, "currency": "EUR"}""")))
        assertEquals("JPY 400", formatPrice(JSONObject("""{"amount": 400, "currency": "JPY"}""")))

    }

    @Test
    fun pricesAreRoundedAndMissingPricesAreBlank() {

        assertEquals("£400", formatPrice(JSONObject("""{"amount": 399.6, "currency": "GBP"}""")))
        assertEquals("£399", formatPrice(JSONObject("""{"amount": 399.4, "currency": "GBP"}""")))
        assertEquals("", formatPrice(JSONObject("""{"amount": 0, "currency": "GBP"}""")))

    }

    // ---------- spotting duplicate flights ----------

    @Test
    fun sameFlightGetsTheSameKeyEvenAtADifferentPrice() {

        val cheap = leg(toLondon, toNewYork)
        val expensive = leg(toLondon, toNewYork)

        assertEquals(legKey(cheap), legKey(expensive))

    }

    @Test
    fun aDifferentDepartureTimeGetsADifferentKey() {

        val later = segment("1301", "ABZ", "2026-12-16T10:00:00", "LHR", "2026-12-16T11:30:00", 90)

        assertNotEquals(legKey(leg(toLondon)), legKey(leg(later)))

    }

}