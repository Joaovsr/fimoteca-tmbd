package com.example.tmdbmovies.feature.movies

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.tmdbmovies.core.ui.theme.TmdbMoviesTheme
import com.example.tmdbmovies.domain.model.Genre
import com.example.tmdbmovies.domain.model.MovieFilters
import com.example.tmdbmovies.domain.model.MovieSortOrder
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
    fun searchEmptyContentHasSpecificMessage() {
        composeRule.setContent { TmdbMoviesTheme { EmptyContent(isSearching = true) } }

        composeRule.onNodeWithText("No movies match this search.").assertExists()
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

    @Test
    fun searchAcceptsTextAndClearRestoresDiscoveryQuery() {
        var latestQuery = ""
        composeRule.setContent {
            var state by remember { mutableStateOf(MoviesUiState()) }
            TmdbMoviesTheme {
                val items = flowOf(PagingData.empty<MovieUiModel>()).collectAsLazyPagingItems()
                MoviesScreen(
                    movies = items,
                    uiState = state,
                    onQueryChanged = {
                        latestQuery = it
                        state = state.copy(query = it)
                    },
                    onMovieClick = {},
                )
            }
        }

        composeRule.onNodeWithTag("movies-search").performTextInput("Alien")
        composeRule.onNodeWithTag("search-clear").performClick()

        assertEquals("", latestQuery)
    }

    @Test
    fun applyingYearShowsRemovableFilterAndClearAllResetsIt() {
        var latestFilters = MovieFilters()
        composeRule.setContent {
            var state by remember { mutableStateOf(MoviesUiState()) }
            TmdbMoviesTheme {
                val items = flowOf(PagingData.empty<MovieUiModel>()).collectAsLazyPagingItems()
                MoviesScreen(
                    movies = items,
                    uiState = state,
                    onFiltersChanged = {
                        latestFilters = it
                        state = state.copy(filters = it)
                    },
                    onClearFilters = {
                        latestFilters = MovieFilters()
                        state = state.copy(filters = MovieFilters())
                    },
                    onMovieClick = {},
                )
            }
        }

        composeRule.onNodeWithTag("filters-open").performClick()
        composeRule.onNodeWithTag("filter-year").performTextInput("2024")
        composeRule.onNodeWithTag("filters-apply").performClick()
        composeRule.onNodeWithText("Year 2024").assertExists()
        composeRule.onNodeWithText("Clear filters").performClick()

        assertEquals(MovieFilters(), latestFilters)
    }

    @Test
    fun searchHidesDiscoverOnlyActiveFilters() {
        val initialState = MoviesUiState(
            query = "Alien",
            filters = MovieFilters(
                genreId = 18,
                sortOrder = MovieSortOrder.VoteAverageDescending,
                minimumRating = 7.0,
                releaseYear = 1979,
            ),
            genres = listOf(Genre(18, "Drama")),
        )
        var latestFilters = initialState.filters
        composeRule.setContent {
            var state by remember { mutableStateOf(initialState) }
            TmdbMoviesTheme {
                val items = flowOf(PagingData.empty<MovieUiModel>()).collectAsLazyPagingItems()
                MoviesScreen(
                    movies = items,
                    uiState = state,
                    onFiltersChanged = {
                        latestFilters = it
                        state = state.copy(filters = it)
                    },
                    onMovieClick = {},
                )
            }
        }

        composeRule.onNodeWithText("Year 1979").assertExists()
        composeRule.onNodeWithText("Drama").assertDoesNotExist()
        composeRule.onNodeWithText(
            "Genre, sort and rating are saved for discovery but are not applied to search results.",
        ).assertExists()
        composeRule.onNodeWithTag("filters-open").performClick()
        composeRule.onNodeWithTag("filter-year").performTextReplacement("1980")
        composeRule.onNodeWithTag("filters-apply").performClick()

        assertEquals(
            MovieFilters(18, MovieSortOrder.VoteAverageDescending, 7.0, 1980),
            latestFilters,
        )
    }
}
