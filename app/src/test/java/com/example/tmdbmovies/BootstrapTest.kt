package com.example.tmdbmovies

import org.junit.Assert.assertNotNull
import org.junit.Test

class BootstrapTest {
    @Test
    fun `TMDB token build field is available without requiring a local secret`() {
        assertNotNull(BuildConfig.TMDB_ACCESS_TOKEN)
    }
}
