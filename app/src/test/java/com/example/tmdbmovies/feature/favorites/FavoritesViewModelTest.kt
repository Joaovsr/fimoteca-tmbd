package com.example.tmdbmovies.feature.favorites

import androidx.lifecycle.SavedStateHandle
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
        val viewModel = FavoritesViewModel(repository, SavedStateHandle())
        backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
        assertEquals("Favorite", viewModel.state.value.movies.single().title)

        viewModel.removeFavorite(42)
        advanceUntilIdle()

        assertEquals(emptyList<FavoriteMovieUiModel>(), viewModel.state.value.movies)
        assertEquals(42L to false, repository.changes.single().let { it.first.movieId to it.second })
    }

    @Test
    fun `search and sort are local and restored`() = runTest(dispatcher) {
        val movies = listOf(
            Movie(1, "Zulu", null, null, null, "1999-01-01", 7.0, emptyList()),
            Movie(2, "Alpha", null, null, null, "2024-01-01", 9.0, emptyList()),
        )
        val handle = SavedStateHandle()
        val viewModel = FavoritesViewModel(FakeFavoriteRepository(movies), handle)
        backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()

        viewModel.onSortOrderChanged(FavoriteSortOrder.TitleAscending)
        viewModel.onQueryChanged("alp")
        advanceUntilIdle()

        assertEquals(listOf("Alpha"), viewModel.state.value.movies.map { it.title })
        assertEquals(2, viewModel.state.value.totalCount)
        val restored = FavoritesViewModel(FakeFavoriteRepository(movies), handle)
        backgroundScope.launch { restored.state.collect {} }
        advanceUntilIdle()
        assertEquals("alp", restored.state.value.query)
        assertEquals(FavoriteSortOrder.TitleAscending, restored.state.value.sortOrder)
    }
}
