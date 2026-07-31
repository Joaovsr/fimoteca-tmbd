package com.example.tmdbmovies.data.repository

import androidx.paging.testing.asSnapshot
import com.example.tmdbmovies.data.remote.TmdbApi
import com.example.tmdbmovies.data.remote.dto.MovieDto
import com.example.tmdbmovies.data.remote.dto.MovieDetailsDto
import com.example.tmdbmovies.data.remote.dto.GenreListDto
import com.example.tmdbmovies.data.remote.dto.PagedResponseDto
import com.example.tmdbmovies.domain.model.MovieFilters
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
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
    fun `blank query uses discover with all supported filters`() = runTest {
        val api = FakeTmdbApi()
        val repository = TmdbMovieRepository(api)

        repository.pagedMovies(
            query = "   ",
            filters = MovieFilters(genreId = 18, minimumRating = 7.5, releaseYear = 2024),
        ).asSnapshot()

        assertEquals(listOf(1, 2), api.discoverRequests.map { it.page })
        assertEquals(listOf(18L, 18L), api.discoverRequests.map { it.genreId })
        assertEquals(listOf(7.5, 7.5), api.discoverRequests.map { it.minimumRating })
        assertEquals(listOf(2024, 2024), api.discoverRequests.map { it.releaseYear })
    }

    @Test
    fun `normalized non blank query uses search and only its compatible year filter`() = runTest {
        val api = FakeTmdbApi()
        val repository = TmdbMovieRepository(api)

        val movies =
            repository.pagedMovies(
                query = "  Alien  ",
                filters = MovieFilters(genreId = 18, minimumRating = 7.5, releaseYear = 1979),
            ).asSnapshot()

        assertEquals(listOf(99L), movies.map { it.movieId })
        assertEquals(1, api.searchRequests.size)
        assertEquals(SearchRequest(query = "Alien", page = 1, releaseYear = 1979), api.searchRequests.single())
        assertEquals(0, api.discoverRequests.size)
    }

    @Test
    fun `each request creates an independent pager flow`() {
        val repository = TmdbMovieRepository(FakeTmdbApi())

        assertNotSame(
            repository.pagedMovies(query = "", filters = MovieFilters()),
            repository.pagedMovies(query = "", filters = MovieFilters()),
        )
    }

    private class FakeTmdbApi : TmdbApi {
        val discoverRequests = mutableListOf<DiscoverRequest>()
        val searchRequests = mutableListOf<SearchRequest>()

        override suspend fun discoverMovies(
            page: Int,
            language: String,
            region: String,
            includeAdult: Boolean,
            sortBy: String?,
            genreId: Long?,
            minimumRating: Double?,
            releaseYear: Int?,
        ): PagedResponseDto<MovieDto> {
            discoverRequests += DiscoverRequest(page, genreId, minimumRating, releaseYear)
            return when (page) {
                1 -> PagedResponseDto(page, listOf(MovieDto(id = 1, title = "One"), MovieDto(id = 2, title = "Two")), 2, 4)
                2 -> PagedResponseDto(page, listOf(MovieDto(id = 3, title = "Three"), MovieDto(id = 4, title = "Four")), 2, 4)
                else -> error("Unexpected page $page")
            }
        }

        override suspend fun searchMovies(
            query: String,
            page: Int,
            language: String,
            includeAdult: Boolean,
            releaseYear: Int?,
        ): PagedResponseDto<MovieDto> {
            searchRequests += SearchRequest(query, page, releaseYear)
            return PagedResponseDto(page, listOf(MovieDto(id = 99, title = query)), 1, 1)
        }

        override suspend fun movieDetails(movieId: Long, language: String): MovieDetailsDto =
            error("Details are not used by paging tests")

        override suspend fun genres(language: String): GenreListDto = error("Genres are not used by paging tests")
    }

    private data class DiscoverRequest(
        val page: Int,
        val genreId: Long?,
        val minimumRating: Double?,
        val releaseYear: Int?,
    )

    private data class SearchRequest(val query: String, val page: Int, val releaseYear: Int?)
}
