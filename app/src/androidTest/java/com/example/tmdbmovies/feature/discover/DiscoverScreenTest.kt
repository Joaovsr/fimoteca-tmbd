package com.example.tmdbmovies.feature.discover

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.performClick
import com.example.tmdbmovies.core.ui.theme.TmdbMoviesTheme
import com.example.tmdbmovies.domain.model.MovieCollection
import com.example.tmdbmovies.feature.movies.MovieUiModel
import org.junit.Assert.assertEquals
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
        composeRule.onNodeWithText("Trending this week").assertExists()
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
        composeRule.onNodeWithText("Try again").performClick()
        assertEquals(MovieCollection.NowPlaying, retried)
    }
}
