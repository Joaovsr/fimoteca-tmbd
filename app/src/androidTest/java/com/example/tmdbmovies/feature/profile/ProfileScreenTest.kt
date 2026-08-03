package com.example.tmdbmovies.feature.profile

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import com.example.tmdbmovies.core.ui.theme.TmdbMoviesTheme
import org.junit.Rule
import org.junit.Test

class ProfileScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsLocalScopeVersionAndTmdbAttribution() {
        composeRule.setContent {
            TmdbMoviesTheme { ProfileScreen(versionName = "1.0-test", favoriteCount = 7) }
        }

        composeRule.onNodeWithText("Your collection").assertExists()
        composeRule.onNodeWithText("7 movies saved locally. Your collection stays on this device.").assertExists()
        composeRule.onNodeWithContentDescription("The Movie Database").assertExists()
        composeRule.onNodeWithText("TMDB Movies · Version 1.0-test").assertExists()
        composeRule.onNodeWithText(
            "This product uses the TMDB API but is not endorsed or certified by TMDB.",
        ).assertExists()
    }
}
