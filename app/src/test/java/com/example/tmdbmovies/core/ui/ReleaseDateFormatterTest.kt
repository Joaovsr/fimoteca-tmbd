package com.example.tmdbmovies.core.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReleaseDateFormatterTest {
    @Test
    fun formatsTmdbDateAsDayMonthYear() {
        assertEquals("15/10/1999", formatReleaseDate("1999-10-15"))
    }

    @Test
    fun rejectsMissingOrMalformedDates() {
        assertNull(formatReleaseDate(null))
        assertNull(formatReleaseDate(""))
        assertNull(formatReleaseDate("15/10/1999"))
    }
}
