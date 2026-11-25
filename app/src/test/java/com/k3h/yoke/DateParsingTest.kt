package com.k3h.yoke

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DateParsingTest {

    @Test
    fun isoDateStaysTheSame() {

        assertEquals("2026-12-16", normaliseDate("2026-12-16"))

    }

    @Test
    fun shortUkDateIsConverted() {

        assertEquals("2026-12-16", normaliseDate("16/12/26"))

    }

    @Test
    fun fourDigitYearIsConverted() {

        assertEquals("2026-12-16", normaliseDate("16/12/2026"))

    }

    @Test
    fun spacesAroundTheDateAreIgnored() {

        assertEquals("2026-12-16", normaliseDate("  2026-12-16  "))

    }

    @Test
    fun dayComesBeforeMonthInSlashDates() {

        // 1 February, not 2 January
        assertEquals("2027-02-01", normaliseDate("1/2/27"))

    }

    @Test
    fun leapDayIsAcceptedInALeapYear() {

        assertEquals("2028-02-29", normaliseDate("29/02/2028"))

    }

    @Test
    fun leapDayIsRejectedInANormalYear() {

        assertNull(normaliseDate("29/02/2027"))

    }

    @Test
    fun impossibleDatesAreRejected() {

        assertNull(normaliseDate("31/02/26"))
        assertNull(normaliseDate("2026-13-01"))

    }

    @Test
    fun textThatIsNotADateIsRejected() {

        assertNull(normaliseDate("banana"))
        assertNull(normaliseDate(""))

    }

}