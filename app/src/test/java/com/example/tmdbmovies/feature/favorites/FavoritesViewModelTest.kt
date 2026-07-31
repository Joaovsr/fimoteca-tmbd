package com.example.tmdbmovies.feature.favorites

import com.example.tmdbmovies.domain.model.Movie
import com.example.tmdbmovies.test.FakeFavoriteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)

    @After fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `observes favorites and removal is reflected immediately`() = runTest(dispatcher) {
        val movie = Movie(42, "  Favorite  ", null, null, null, "2026-07-31", null, emptyList())
        val repository = FakeFavoriteRepository(listOf(movie))
        val viewModel = FavoritesViewModel(repository)
        backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertEquals("Favorite", viewModel.state.value.movies.single().title)

        viewModel.removeFavorite(42)
        advanceUntilIdle()

        assertEquals(emptyList<FavoriteMovieUiModel>(), viewModel.state.value.movies)
        assertEquals(42L to false, repository.changes.single().let { it.first.movieId to it.second })
    }
}
