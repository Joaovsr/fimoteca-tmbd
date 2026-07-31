package com.example.tmdbmovies.feature.movies

import androidx.paging.PagingData
import androidx.lifecycle.viewModelScope
import com.example.tmdbmovies.domain.model.Movie
import com.example.tmdbmovies.domain.model.MovieFilters
import com.example.tmdbmovies.domain.repository.MovieRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MoviesViewModelTest {
    @Test
    fun `maps repository movies into UI models while keeping identifier and fallbacks input`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val repository = FakeMovieRepository(
            listOf(
                Movie(7, "  Movie title  ", null, "/poster.jpg", null, " 2026-07-31 ", null, emptyList()),
                Movie(9, "", null, null, null, " ", null, emptyList()),
            ),
        )

        val viewModel = MoviesViewModel(repository)
        try {
            assertNotNull(viewModel.movies.first())

            assertEquals(MovieFilters(), repository.filters)
            assertEquals("", repository.query)
            assertEquals(
                listOf(
                    MovieUiModel(7, "Movie title", "2026-07-31", "/poster.jpg"),
                    MovieUiModel(9, "", null, null),
                ),
                repository.movies.map(Movie::toUiModel),
            )
        } finally {
            viewModel.viewModelScope.cancel()
            Dispatchers.resetMain()
        }
    }

    private class FakeMovieRepository(val movies: List<Movie>) : MovieRepository {
        var query: String? = null
        var filters: MovieFilters? = null

        override fun pagedMovies(query: String, filters: MovieFilters): Flow<PagingData<Movie>> {
            this.query = query
            this.filters = filters
            return flowOf(PagingData.from(movies))
        }
    }
}
