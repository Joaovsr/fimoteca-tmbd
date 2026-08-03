package com.example.tmdbmovies.feature.discover

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.tmdbmovies.core.ui.theme.TmdbMoviesTheme
import com.example.tmdbmovies.domain.model.MovieCollection
import com.example.tmdbmovies.feature.movies.MovieUiModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class DiscoverScreenTest {
    @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun contentShowsBannerSectionsAndFavoriteAction() {
        var favoriteId: Long? = null
        val movie = MovieUiModel(42, "Cinema Movie", "2024-01-01", "/poster.jpg", backdropPath = "/backdrop.jpg", voteAverage = 8.4)
        composeRule.setContent {
            TmdbMoviesTheme {
                DiscoverScreen(
                    state = DiscoverUiState(MovieCollection.entries.associateWith { DiscoverSectionState(listOf(movie), false) }),
                    onMovieClick = {}, onFavoriteClick = { favoriteId = it.id }, onRetry = {}, onFeaturedSelected = {}, onSearchClick = {},
                )
            }
        }

        composeRule.onNodeWithTag("featured-movie").assertExists()
        composeRule.onNodeWithText("Em alta nesta semana").assertExists()
        composeRule.onAllNodesWithTag("discover-favorite-42")[0].performClick()
        assertEquals(42L, favoriteId)
    }

    @Test
    fun failedSectionRetriesWithoutHidingSuccessfulSection() {
        var retried: MovieCollection? = null
        val sections = MovieCollection.entries.associateWith { collection ->
            if (collection == MovieCollection.NowPlaying) DiscoverSectionState(isLoading = false, errorMessageRes = com.example.tmdbmovies.R.string.error_no_connection)
            else DiscoverSectionState(listOf(MovieUiModel(collection.ordinal.toLong() + 1, collection.name, null, null)), false)
        }
        composeRule.setContent {
            TmdbMoviesTheme {
                DiscoverScreen(DiscoverUiState(sections), {}, {}, { retried = it }, {}, {})
            }
        }

        composeRule.onNodeWithTag("featured-movie").assertExists()
        composeRule.onNodeWithText("Tentar novamente").performClick()
        assertEquals(MovieCollection.NowPlaying, retried)
    }

    @Test
    fun featuredMoviesCanBeChangedWithHorizontalSwipe() {
        var openedMovieId: Long? = null
        setFeaturedMoviesContent(onMovieClick = { openedMovieId = it })

        composeRule.onNodeWithTag("featured-pager").performTouchInput { swipeLeft() }

        composeRule.onNodeWithTag("featured-movie-2").assertIsDisplayed()
        assertNull(openedMovieId)
    }

    @Test
    fun featuredMoviesAdvanceAutomatically() {
        composeRule.mainClock.autoAdvance = false
        setFeaturedMoviesContent()

        composeRule.mainClock.advanceTimeBy(6_000)
        composeRule.mainClock.advanceTimeByFrame()

        composeRule.onNodeWithTag("featured-movie-2").assertIsDisplayed()
    }

    private fun setFeaturedMoviesContent(onMovieClick: (Long) -> Unit = {}) {
        val featuredMovies = listOf(
            MovieUiModel(1, "First featured", "2025-01-01", null),
            MovieUiModel(2, "Second featured", "2026-01-01", null),
        )
        val sections = MovieCollection.entries.associateWith { collection ->
            DiscoverSectionState(
                movies = if (collection == MovieCollection.TrendingWeekly) featuredMovies else emptyList(),
                isLoading = false,
            )
        }
        composeRule.setContent {
            var state by remember { mutableStateOf(DiscoverUiState(sections)) }
            TmdbMoviesTheme {
                DiscoverScreen(
                    state = state,
                    onMovieClick = onMovieClick,
                    onFavoriteClick = {},
                    onRetry = {},
                    onFeaturedSelected = { state = state.copy(selectedFeaturedIndex = it) },
                    onSearchClick = {},
                )
            }
        }
    }
}
