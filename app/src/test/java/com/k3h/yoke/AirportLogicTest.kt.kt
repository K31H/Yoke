package com.k3h.yoke

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AirportLogicTest {

    // ---------- searching the built-in airport list ----------

    @Test
    fun searchFindsAnAirportByCityName() {

        assertEquals("ABZ", searchAirports("aberdeen").first().code)

    }

    @Test
    fun searchIsNotCaseSensitive() {

        assertEquals("ABZ", searchAirports("ABERDEEN").first().code)

    }

    @Test
    fun searchFindsAnAirportByCode() {

        assertEquals("JFK", searchAirports("jfk").first().code)

    }

    @Test
    fun searchForACityWithSeveralAirportsReturnsThem() {

        val results = searchAirports("london", limit = 5)

        assertTrue(results.size > 1)
        assertTrue(results.all { it.name.startsWith("London") })

    }

    @Test
    fun searchRespectsTheLimit() {

        assertEquals(2, searchAirports("london", limit = 2).size)

    }

    @Test
    fun searchGivesNothingForShortOrUnknownText() {

        assertTrue(searchAirports("a").isEmpty())
        assertTrue(searchAirports("").isEmpty())
        assertTrue(searchAirports("zzzzzz").isEmpty())

    }

    @Test
    fun searchFindsOrlando() {

        // needs the extra airports (MCO) to be in Airports.kt
        assertEquals("MCO", searchAirports("orlando").first().code)

    }

    // ---------- showing airport names ----------

    @Test
    fun labelShowsTheNameAndCode() {

        assertEquals("Aberdeen (ABZ)", airportLabel("ABZ"))

    }

    @Test
    fun labelFallsBackToTheCodeForUnknownAirports() {

        assertEquals("ZZZ", airportLabel("ZZZ"))

    }

    // ---------- turning typed text into an airport code ----------

    @Test
    fun resolveReadsTheCodeFromASuggestionLabel() {

        runBlocking {

            assertEquals("ABZ", resolveAirportCode("Aberdeen (ABZ)"))
            assertEquals("MCO", resolveAirportCode("Orlando (MCO)"))

        }

    }

    @Test
    fun resolveAcceptsALowercaseCode() {

        runBlocking {

            assertEquals("ABZ", resolveAirportCode("abz"))

        }

    }

    @Test
    fun resolveIgnoresSpacesAroundACode() {

        runBlocking {

            assertEquals("JFK", resolveAirportCode("  jfk  "))

        }

    }

    @Test
    fun resolveFindsACityInTheBuiltInList() {

        runBlocking {

            assertEquals("ABZ", resolveAirportCode("Aberdeen"))

        }

    }

    @Test
    fun resolveGivesNullForBlankText() {

        runBlocking {

            assertNull(resolveAirportCode(""))
            assertNull(resolveAirportCode("   "))

        }

    }

    // ---------- distances and the nearest airport ----------

    @Test
    fun distanceBetweenTheSamePointIsZero() {

        assertEquals(0.0, distanceKm(51.47, -0.45, 51.47, -0.45), 0.001)

    }

    @Test
    fun distanceFromLondonToParisIsAbout343km() {

        val km = distanceKm(51.5074, -0.1278, 48.8566, 2.3522)

        assertEquals(343.5, km, 5.0)

    }

    @Test
    fun distanceIsTheSameInBothDirections() {

        val there = distanceKm(57.2, -2.2, 40.6, -73.8)
        val back = distanceKm(40.6, -73.8, 57.2, -2.2)

        assertEquals(there, back, 0.001)

    }

    @Test
    fun nearestAirportToAberdeenCityCentreIsAberdeenAirport() {

        val match = nearestAirport(57.1497, -2.0943)

        assertEquals("ABZ", match.airport.code)
        assertTrue(match.distanceKm < 15f)

    }

    @Test
    fun nearestAirportWhenStandingAtAnAirportIsThatAirport() {

        val jfk = AIRPORTS.first { it.code == "JFK" }
        val match = nearestAirport(jfk.lat, jfk.lon)

        assertEquals("JFK", match.airport.code)
        assertEquals(0f, match.distanceKm, 0.01f)

    }

    @Test
    fun nearestAirportWorksInTheSouthernHemisphere() {

        // Sydney city centre: negative latitude
        assertEquals("SYD", nearestAirport(-33.8688, 151.2093).airport.code)

    }

    @Test
    fun nearestAirportToDowntownOrlandoIsOrlandoInternational() {

        // needs the extra airports (MCO) to be in Airports.kt
        assertEquals("MCO", nearestAirport(28.5383, -81.3792).airport.code)

    }

}