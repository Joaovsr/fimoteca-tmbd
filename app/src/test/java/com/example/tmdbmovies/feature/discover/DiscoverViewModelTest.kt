package com.example.tmdbmovies.feature.discover

import androidx.lifecycle.SavedStateHandle
import androidx.paging.PagingData
import com.example.tmdbmovies.core.common.AppError
import com.example.tmdbmovies.core.common.AppResult
import com.example.tmdbmovies.domain.model.Genre
import com.example.tmdbmovies.domain.model.Movie
import com.example.tmdbmovies.domain.model.MovieCollection
import com.example.tmdbmovies.domain.model.MovieDetails
import com.example.tmdbmovies.domain.model.MovieFilters
import com.example.tmdbmovies.domain.repository.MovieRepository
import com.example.tmdbmovies.test.FakeFavoriteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DiscoverViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `loads sections independently and retry replaces only failed section`() = runTest(dispatcher) {
        val movie = movie(1, "Trending")
        val repository = FakeMovieRepository(
            mutableMapOf(
                MovieCollection.TrendingWeekly to AppResult.Success(listOf(movie)),
                MovieCollection.NowPlaying to AppResult.Failure(AppError.NoConnection),
                MovieCollection.Classics to AppResult.Success(emptyList()),
                MovieCollection.TopRated to AppResult.Success(listOf(movie(2, "Rated"))),
            ),
        )
        val viewModel = DiscoverViewModel(repository, FakeFavoriteRepository(), SavedStateHandle())
        backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()

        assertEquals("Trending", viewModel.state.value.featuredMovie?.title)
        assertEquals(1, viewModel.state.value.sections.getValue(MovieCollection.TrendingWeekly).movies.size)
        assertEquals(com.example.tmdbmovies.R.string.error_no_connection, viewModel.state.value.sections.getValue(MovieCollection.NowPlaying).errorMessageRes)
        assertTrue(viewModel.state.value.sections.getValue(MovieCollection.Classics).movies.isEmpty())
        assertFalse(viewModel.state.value.sections.getValue(MovieCollection.TopRated).isLoading)

        repository.results[MovieCollection.NowPlaying] = AppResult.Success(listOf(movie(3, "Cinema")))
        viewModel.retry(MovieCollection.NowPlaying)
        advanceUntilIdle()

        assertEquals("Cinema", viewModel.state.value.sections.getValue(MovieCollection.NowPlaying).movies.single().title)
        assertEquals(2, repository.calls.count { it == MovieCollection.NowPlaying })
        assertEquals(1, repository.calls.count { it == MovieCollection.TrendingWeekly })
    }

    @Test
    fun `favorite state is reactive and featured selection is saved`() = runTest(dispatcher) {
        val first = movie(1, "First")
        val second = movie(2, "Second")
        val favorites = FakeFavoriteRepository()
        val handle = SavedStateHandle()
        val repository = FakeMovieRepository(MovieCollection.entries.associateWith { AppResult.Success(listOf(first, second)) }.toMutableMap())
        val viewModel = DiscoverViewModel(repository, favorites, handle)
        backgroundScope.launch { viewModel.state.collect {} }
        advanceUntilIdle()

        viewModel.selectFeatured(1)
        viewModel.onFavoriteClick(viewModel.state.value.featuredMovie!!)
        advanceUntilIdle()

        assertEquals("Second", viewModel.state.value.featuredMovie?.title)
        assertTrue(viewModel.state.value.featuredMovie?.isFavorite == true)
        assertEquals(1, handle.get<Int>("discover.featured.index"))
    }

    private class FakeMovieRepository(
        val results: MutableMap<MovieCollection, AppResult<List<Movie>>>,
    ) : MovieRepository {
        val calls = mutableListOf<MovieCollection>()
        override suspend fun movies(collection: MovieCollection): AppResult<List<Movie>> {
            calls += collection
            return results.getValue(collection)
        }
        override fun pagedMovies(query: String, filters: MovieFilters): Flow<PagingData<Movie>> = emptyFlow()
        override suspend fun movieDetails(movieId: Long): AppResult<MovieDetails> = AppResult.Failure(AppError.Unknown)
        override suspend fun genres(): AppResult<List<Genre>> = AppResult.Success(emptyList())
    }

    private fun movie(id: Long, title: String) = Movie(id, title, null, "/poster.jpg", "/backdrop.jpg", "1999-01-01", 8.2, emptyList())
}
