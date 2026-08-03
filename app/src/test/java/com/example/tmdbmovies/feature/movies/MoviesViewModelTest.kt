package com.example.tmdbmovies.feature.movies

import androidx.lifecycle.SavedStateHandle
import androidx.paging.PagingData
import com.example.tmdbmovies.R
import com.example.tmdbmovies.core.common.AppError
import com.example.tmdbmovies.core.common.AppResult
import com.example.tmdbmovies.domain.model.Genre
import com.example.tmdbmovies.domain.model.Movie
import com.example.tmdbmovies.domain.model.MovieDetails
import com.example.tmdbmovies.domain.model.MovieFilters
import com.example.tmdbmovies.domain.model.MovieSortOrder
import com.example.tmdbmovies.domain.repository.MovieRepository
import com.example.tmdbmovies.test.FakeFavoriteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MoviesViewModelTest {
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
    fun `initial discover maps movies and loads sorted genres`() = runTest(dispatcher) {
        val repository = FakeMovieRepository(
            movies = listOf(Movie(7, "  Movie title  ", null, "/poster.jpg", null, " 2026-07-31 ", null, emptyList())),
            genreResults = mutableListOf(AppResult.Success(listOf(Genre(18, "Drama"), Genre(28, "Action")))),
        )
        val viewModel = MoviesViewModel(repository, FakeFavoriteRepository(), SavedStateHandle())

        backgroundScope.launch { viewModel.movies.collect {} }
        advanceUntilIdle()

        assertEquals(
            listOf(MovieUiModel(7, "Movie title", "2026-07-31", "/poster.jpg")),
            repository.movies.map(Movie::toUiModel),
        )
        assertEquals(Request("", MovieFilters()), repository.requests.single())
        assertEquals(listOf("Action", "Drama"), viewModel.state.value.genres.map { it.name })
        assertFalse(viewModel.state.value.isLoadingGenres)
    }

    @Test
    fun `search is debounced and only the latest query starts a pager`() = runTest(dispatcher) {
        val repository = FakeMovieRepository()
        val viewModel = MoviesViewModel(repository, FakeFavoriteRepository(), SavedStateHandle())
        backgroundScope.launch { viewModel.movies.collect {} }
        runCurrent()

        viewModel.onQueryChanged("Alien")
        advanceTimeBy(399)
        runCurrent()
        assertEquals(listOf(""), repository.requests.map { it.query })

        viewModel.onQueryChanged("Aliens")
        advanceTimeBy(MoviesViewModel.SEARCH_DEBOUNCE_MILLIS)
        runCurrent()
        assertEquals(listOf("", "Aliens"), repository.requests.map { it.query })

        viewModel.onQueryChanged("   ")
        runCurrent()
        assertEquals(listOf("", "Aliens", ""), repository.requests.map { it.query })
    }

    @Test
    fun `new effective request cancels obsolete repository flow`() = runTest(dispatcher) {
        val repository = CancellableRepository()
        val viewModel = MoviesViewModel(repository, FakeFavoriteRepository(), SavedStateHandle())
        backgroundScope.launch { viewModel.movies.collect {} }
        runCurrent()

        viewModel.onQueryChanged("Alien")
        advanceTimeBy(MoviesViewModel.SEARCH_DEBOUNCE_MILLIS)
        runCurrent()
        viewModel.onQueryChanged("Aliens")
        advanceTimeBy(MoviesViewModel.SEARCH_DEBOUNCE_MILLIS)
        runCurrent()

        assertEquals(listOf("", "Alien", "Aliens"), repository.started)
        assertTrue(repository.cancelled.contains("Alien"))
    }

    @Test
    fun `search hides incompatible filters and only effective changes restart paging`() = runTest(dispatcher) {
        val repository = FakeMovieRepository()
        val viewModel = MoviesViewModel(repository, FakeFavoriteRepository(), SavedStateHandle())
        backgroundScope.launch { viewModel.movies.collect {} }
        runCurrent()

        viewModel.onFiltersChanged(
            MovieFilters(18, MovieSortOrder.VoteAverageDescending, 7.0, 2024),
        )
        runCurrent()
        viewModel.onQueryChanged("Alien")
        advanceTimeBy(MoviesViewModel.SEARCH_DEBOUNCE_MILLIS)
        runCurrent()

        val searchRequest = repository.requests.last()
        assertEquals("Alien", searchRequest.query)
        assertEquals(MovieFilters(releaseYear = 2024), searchRequest.filters)

        val count = repository.requests.size
        viewModel.onGenreChanged(28)
        runCurrent()
        assertEquals(count, repository.requests.size)

        viewModel.onReleaseYearChanged(2025)
        runCurrent()
        assertEquals(MovieFilters(releaseYear = 2025), repository.requests.last().filters)
    }

    @Test
    fun `query and filters are restored from SavedStateHandle`() = runTest(dispatcher) {
        val handle = SavedStateHandle()
        val first = MoviesViewModel(FakeMovieRepository(), FakeFavoriteRepository(), handle)
        first.onQueryChanged("Matrix")
        first.onFiltersChanged(MovieFilters(878, MovieSortOrder.ReleaseDateDescending, 5.0, 1999))
        advanceUntilIdle()

        val restored = MoviesViewModel(FakeMovieRepository(), FakeFavoriteRepository(), handle)
        advanceUntilIdle()

        assertEquals("Matrix", restored.state.value.query)
        assertEquals(MovieFilters(878, MovieSortOrder.ReleaseDateDescending, 5.0, 1999), restored.state.value.filters)
    }

    @Test
    fun `genre error is independent and retry replaces it with content`() = runTest(dispatcher) {
        val repository = FakeMovieRepository(
            genreResults = mutableListOf(
                AppResult.Failure(AppError.NoConnection),
                AppResult.Success(listOf(Genre(18, "Drama"))),
            ),
        )
        val viewModel = MoviesViewModel(repository, FakeFavoriteRepository(), SavedStateHandle())
        advanceUntilIdle()

        assertEquals(R.string.error_no_connection, viewModel.state.value.genresErrorMessageRes)
        assertNull(viewModel.state.value.genres.singleOrNull())

        viewModel.retryGenres()
        advanceUntilIdle()

        assertNull(viewModel.state.value.genresErrorMessageRes)
        assertEquals("Drama", viewModel.state.value.genres.single().name)
    }

    @Test
    fun `favorite action writes the complete movie and requested state`() = runTest(dispatcher) {
        val favorites = FakeFavoriteRepository()
        val viewModel = MoviesViewModel(FakeMovieRepository(), favorites, SavedStateHandle())
        val uiMovie = MovieUiModel(
            id = 9,
            title = "Movie",
            releaseDate = "2026-07-31",
            posterPath = "/poster",
            overview = "Overview",
            genreIds = listOf(18),
        )

        viewModel.onFavoriteClick(uiMovie)
        advanceUntilIdle()

        val change = favorites.changes.single()
        assertEquals(9L, change.first.movieId)
        assertEquals("Overview", change.first.overview)
        assertEquals(listOf(18L), change.first.genreIds)
        assertTrue(change.second)
    }

    @Test
    fun `favorite flow remaps loaded page without another remote request`() = runTest(dispatcher) {
        val movie = Movie(9, "Movie", null, null, null, null, null, emptyList())
        val repository = FakeMovieRepository(movies = listOf(movie))
        val favorites = FakeFavoriteRepository()
        val viewModel = MoviesViewModel(repository, favorites, SavedStateHandle())
        val emissions = mutableListOf<PagingData<MovieUiModel>>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.movies.take(2).collect(emissions::add)
        }
        runCurrent()

        viewModel.onFavoriteClick(movie.toUiModel())
        runCurrent()

        assertEquals(2, emissions.size)
        assertTrue(movie.toUiModel(isFavorite = true).isFavorite)
        assertEquals(1, repository.requests.size)
    }

    private data class Request(val query: String, val filters: MovieFilters)

    private class FakeMovieRepository(
        val movies: List<Movie> = emptyList(),
        private val genreResults: MutableList<AppResult<List<Genre>>> = mutableListOf(AppResult.Success(emptyList())),
    ) : MovieRepository {
        val requests = mutableListOf<Request>()

        override fun pagedMovies(query: String, filters: MovieFilters): Flow<PagingData<Movie>> {
            requests += Request(query, filters)
            return flowOf(PagingData.from(movies))
        }

        override suspend fun movies(collection: com.example.tmdbmovies.domain.model.MovieCollection): AppResult<List<Movie>> = AppResult.Success(emptyList())

        override suspend fun genres(): AppResult<List<Genre>> =
            if (genreResults.size > 1) genreResults.removeAt(0) else genreResults.first()

        override suspend fun movieDetails(movieId: Long): AppResult<MovieDetails> =
            error("Details are not used by movies tests")
    }

    private class CancellableRepository : MovieRepository {
        val started = mutableListOf<String>()
        val cancelled = mutableListOf<String>()

        override fun pagedMovies(query: String, filters: MovieFilters): Flow<PagingData<Movie>> = flow {
            started += query
            try {
                emit(PagingData.empty())
                awaitCancellation()
            } finally {
                cancelled += query
            }
        }

        override suspend fun movies(collection: com.example.tmdbmovies.domain.model.MovieCollection): AppResult<List<Movie>> = AppResult.Success(emptyList())

        override suspend fun genres(): AppResult<List<Genre>> = AppResult.Success(emptyList())

        override suspend fun movieDetails(movieId: Long): AppResult<MovieDetails> =
            error("Details are not used by movies tests")
    }
}
