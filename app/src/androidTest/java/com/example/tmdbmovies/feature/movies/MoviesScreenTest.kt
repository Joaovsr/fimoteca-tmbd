package com.example.tmdbmovies.feature.movies

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.tmdbmovies.core.ui.theme.TmdbMoviesTheme
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class MoviesScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun emptyContentIsVisible() {
        composeRule.setContent { TmdbMoviesTheme { EmptyContent() } }

        composeRule.onNodeWithTag("movies-empty").assertExists()
        composeRule.onNodeWithText("No movies found.").assertExists()
    }

    @Test
    fun movieCardShowsFallbackAndSendsOnlyMovieIdOnClick() {
        var selectedId: Long? = null
        val movie = MovieUiModel(id = 42, title = "Movie title", releaseDate = null, posterPath = null)
        composeRule.setContent {
            TmdbMoviesTheme {
                val items = flowOf(PagingData.from(listOf(movie))).collectAsLazyPagingItems()
                MoviesScreen(items, onMovieClick = { selectedId = it })
            }
        }

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Movie title").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("movie-poster-fallback", useUnmergedTree = true).assertExists()
        composeRule.onNodeWithContentDescription("Poster for Movie title").assertExists()
        composeRule.onNodeWithTag("movie-card-42").performClick()

        assertEquals(42L, selectedId)
    }

    @Test
    fun errorRetryComponentInvokesRetry() {
        var retries = 0
        composeRule.setContent {
            TmdbMoviesTheme { MoviesErrorContent("Could not load movies", onRetry = { retries++ }) }
        }

        composeRule.onNodeWithTag("movies-retry").performClick()

        assertEquals(1, retries)
    }
}
