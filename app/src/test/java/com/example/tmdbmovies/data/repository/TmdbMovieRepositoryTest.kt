package com.example.tmdbmovies.data.repository

import androidx.paging.testing.asSnapshot
import com.example.tmdbmovies.data.remote.TmdbApi
import com.example.tmdbmovies.data.remote.dto.MovieDto
import com.example.tmdbmovies.data.remote.dto.MovieDetailsDto
import com.example.tmdbmovies.data.remote.dto.PagedResponseDto
import com.example.tmdbmovies.domain.model.MovieFilters
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class TmdbMovieRepositoryTest {
    @Test
    fun `repository pager loads multiple pages through its paging source factory`() = runTest {
        val repository = TmdbMovieRepository(FakeTmdbApi())

        val movies =
            repository.pagedMovies(query = "", filters = MovieFilters()).asSnapshot {
                appendScrollWhile { movie -> movie.movieId != 4L }
            }

        assertEquals(listOf(1L, 2L, 3L, 4L), movies.map { it.movieId })
    }

    @Test
    fun `non blank query fails until search endpoint is implemented`() {
        val repository = TmdbMovieRepository(FakeTmdbApi())

        assertThrows(IllegalArgumentException::class.java) {
            repository.pagedMovies(query = "Alien", filters = MovieFilters())
        }
    }

    private class FakeTmdbApi : TmdbApi {
        override suspend fun discoverMovies(
            page: Int,
            language: String,
            region: String,
            includeAdult: Boolean,
            sortBy: String?,
            genreId: Long?,
            minimumRating: Double?,
            releaseYear: Int?,
        ): PagedResponseDto<MovieDto> =
            when (page) {
                1 -> PagedResponseDto(page, listOf(MovieDto(id = 1, title = "One"), MovieDto(id = 2, title = "Two")), 2, 4)
                2 -> PagedResponseDto(page, listOf(MovieDto(id = 3, title = "Three"), MovieDto(id = 4, title = "Four")), 2, 4)
                else -> error("Unexpected page $page")
            }

        override suspend fun movieDetails(movieId: Long, language: String): MovieDetailsDto =
            error("Details are not used by paging tests")
    }
}
