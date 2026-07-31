package com.example.tmdbmovies.data.repository

import com.example.tmdbmovies.core.common.AppError
import com.example.tmdbmovies.core.common.AppResult
import com.example.tmdbmovies.data.remote.TmdbApi
import com.example.tmdbmovies.data.remote.dto.GenreDto
import com.example.tmdbmovies.data.remote.dto.GenreListDto
import com.example.tmdbmovies.data.remote.dto.MovieDetailsDto
import com.example.tmdbmovies.data.remote.dto.MovieDto
import com.example.tmdbmovies.data.remote.dto.PagedResponseDto
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class TmdbMovieGenresRepositoryTest {
    @Test
    fun `genres maps valid values and accepts an empty successful response`() = runTest {
        val mapped = TmdbMovieRepository(FakeTmdbApi { GenreListDto(listOf(GenreDto(28, " Ação "), GenreDto(null, "Invalid"))) }).genres()
        val empty = TmdbMovieRepository(FakeTmdbApi { GenreListDto() }).genres()

        assertEquals(AppResult.Success(listOf(com.example.tmdbmovies.domain.model.Genre(28, "Ação"))), mapped)
        assertEquals(AppResult.Success<List<com.example.tmdbmovies.domain.model.Genre>>(emptyList()), empty)
    }

    @Test
    fun `genres converts failures to typed errors`() = runTest {
        val result = TmdbMovieRepository(FakeTmdbApi { throw IOException("offline") }).genres()

        assertEquals(AppResult.Failure(AppError.NoConnection), result)
    }

    private class FakeTmdbApi(
        private val genreResponse: suspend () -> GenreListDto,
    ) : TmdbApi {
        override suspend fun genres(language: String): GenreListDto = genreResponse()

        override suspend fun discoverMovies(page: Int, language: String, region: String, includeAdult: Boolean, sortBy: String?, genreId: Long?, minimumRating: Double?, releaseYear: Int?): PagedResponseDto<MovieDto> =
            error("Discover is not used by genre tests")

        override suspend fun searchMovies(query: String, page: Int, language: String, includeAdult: Boolean, releaseYear: Int?): PagedResponseDto<MovieDto> =
            error("Search is not used by genre tests")

        override suspend fun movieDetails(movieId: Long, language: String): MovieDetailsDto = error("Details are not used by genre tests")
    }
}
