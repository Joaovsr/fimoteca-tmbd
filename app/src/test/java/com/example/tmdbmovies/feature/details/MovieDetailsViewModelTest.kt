package com.example.tmdbmovies.feature.details

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import androidx.paging.PagingData
import com.example.tmdbmovies.R
import com.example.tmdbmovies.core.common.AppError
import com.example.tmdbmovies.core.common.AppResult
import com.example.tmdbmovies.domain.model.Movie
import com.example.tmdbmovies.domain.model.MovieDetails
import com.example.tmdbmovies.domain.model.MovieFilters
import com.example.tmdbmovies.domain.model.Genre
import com.example.tmdbmovies.domain.repository.MovieRepository
import com.example.tmdbmovies.test.FakeFavoriteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MovieDetailsViewModelTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loads requested movie and maps blank fields for UI fallback`() = runTest(dispatcher) {
        val repository = FakeRepository(
            mutableListOf(
                AppResult.Success(MovieDetails(42, "  Movie  ", "  ", null, null, "")),
            ),
        )
        val viewModel = MovieDetailsViewModel(repository, FakeFavoriteRepository(), SavedStateHandle(mapOf("movieId" to 42L)))

        assertEquals(MovieDetailsUiState.Loading, viewModel.state.value)
        advanceUntilIdle()

        assertEquals(listOf(42L), repository.requests)
        val content = viewModel.state.value as MovieDetailsUiState.Content
        assertEquals("Movie", content.movie.title)
        assertEquals(null, content.movie.overview)
        assertEquals(null, content.movie.releaseDate)
    }

    @Test
    fun `failure exposes localized error and retry replaces it with content`() = runTest(dispatcher) {
        val repository = FakeRepository(
            mutableListOf(
                AppResult.Failure(AppError.NoConnection),
                AppResult.Success(MovieDetails(42, "Recovered", null, null, null, null)),
            ),
        )
        val viewModel = MovieDetailsViewModel(repository, FakeFavoriteRepository(), SavedStateHandle(mapOf("movieId" to 42L)))

        advanceUntilIdle()
        assertEquals(MovieDetailsUiState.Error(R.string.error_no_connection), viewModel.state.value)

        viewModel.retry()
        assertEquals(MovieDetailsUiState.Loading, viewModel.state.value)
        advanceUntilIdle()
        assertTrue(viewModel.state.value is MovieDetailsUiState.Content)
        assertEquals(2, repository.requests.size)
    }

    @Test
    fun `favorite action persists details and room emission updates content`() = runTest(dispatcher) {
        val repository = FakeRepository(
            mutableListOf(
                AppResult.Success(MovieDetails(42, "Movie", "Overview", "/poster", "/backdrop", "2026-07-31", 8.2)),
            ),
        )
        val favorites = FakeFavoriteRepository()
        val viewModel = MovieDetailsViewModel(repository, favorites, SavedStateHandle(mapOf("movieId" to 42L)))
        advanceUntilIdle()

        viewModel.onFavoriteClick()
        advanceUntilIdle()

        val change = favorites.changes.single()
        assertEquals(42L, change.first.movieId)
        assertEquals(8.2, change.first.voteAverage)
        assertTrue(change.second)
        assertTrue((viewModel.state.value as MovieDetailsUiState.Content).isFavorite)
    }

    private class FakeRepository(
        private val results: MutableList<AppResult<MovieDetails>>,
    ) : MovieRepository {
        val requests = mutableListOf<Long>()

        override fun pagedMovies(query: String, filters: MovieFilters): Flow<PagingData<Movie>> = emptyFlow()

        override suspend fun movies(collection: com.example.tmdbmovies.domain.model.MovieCollection): AppResult<List<Movie>> = AppResult.Success(emptyList())

        override suspend fun genres(): AppResult<List<Genre>> = AppResult.Success(emptyList())

        override suspend fun movieDetails(movieId: Long): AppResult<MovieDetails> {
            requests += movieId
            return results.removeAt(0)
        }
    }
}
