package com.example.tmdbmovies.feature.favorites

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.tmdbmovies.core.ui.theme.TmdbMoviesTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class FavoritesScreenTest {
    @get:Rule val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun emptyStateIsVisible() {
        composeRule.setContent {
            TmdbMoviesTheme {
                FavoritesScreen(FavoritesUiState(isLoading = false), {}, {})
            }
        }

        composeRule.onNodeWithTag("favorites-empty").assertExists()
        composeRule.onNodeWithText("Os filmes marcados com um coração aparecerão aqui.").assertExists()
    }

    @Test
    fun contentNavigatesAndCanBeRemoved() {
        var selectedId: Long? = null
        var removedId: Long? = null
        val movie = FavoriteMovieUiModel(42, "Movie", null, null)
        composeRule.setContent {
            TmdbMoviesTheme {
                FavoritesScreen(
                    FavoritesUiState(movies = listOf(movie), totalCount = 1, isLoading = false),
                    onMovieClick = { selectedId = it },
                    onRemoveClick = { removedId = it },
                )
            }
        }

        composeRule.onNodeWithTag("favorite-card-42").performClick()
        composeRule.onNodeWithTag("favorite-remove-42").performClick()

        assertEquals(42L, selectedId)
        assertEquals(42L, removedId)
    }

    @Test
    fun emptyStateExploresMovies() {
        var explored = false
        composeRule.setContent {
            TmdbMoviesTheme {
                FavoritesScreen(
                    FavoritesUiState(isLoading = false), {}, {},
                    onExploreClick = { explored = true },
                )
            }
        }

        composeRule.onNodeWithTag("favorites-explore").performClick()
        assertEquals(true, explored)
    }

    @Test
    fun sortSheetShowsLocalOptions() {
        composeRule.setContent {
            TmdbMoviesTheme {
                FavoritesScreen(
                    FavoritesUiState(
                        movies = listOf(
                            FavoriteMovieUiModel(1, "One", null, null),
                            FavoriteMovieUiModel(2, "Two", null, null),
                        ),
                        totalCount = 2,
                        isLoading = false,
                    ), {}, {},
                )
            }
        }
        composeRule.onNodeWithTag("favorites-sort").performClick()
        composeRule.onNodeWithText("Maior nota").assertExists()
    }
}
