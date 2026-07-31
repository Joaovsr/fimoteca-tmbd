package com.example.tmdbmovies.data.repository

import com.example.tmdbmovies.core.common.AppError
import com.example.tmdbmovies.core.common.AppResult
import com.example.tmdbmovies.data.remote.TmdbApi
import com.example.tmdbmovies.data.remote.dto.MovieDetailsDto
import com.example.tmdbmovies.data.remote.dto.MovieDto
import com.example.tmdbmovies.data.remote.dto.PagedResponseDto
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TmdbMovieDetailsRepositoryTest {
    @Test
    fun `details map into domain and preserve requested id`() = runTest {
        val api = FakeTmdbApi { id -> MovieDetailsDto(id = id, title = "Movie", posterPath = "poster.jpg") }

        val result = TmdbMovieRepository(api).movieDetails(42)

        assertEquals(listOf(42L), api.detailRequests)
        assertTrue(result is AppResult.Success)
        assertEquals("/poster.jpg", (result as AppResult.Success).value.posterPath)
    }

    @Test
    fun `missing id and network exception become typed failures`() = runTest {
        val missingId = TmdbMovieRepository(FakeTmdbApi { MovieDetailsDto(title = "Invalid") }).movieDetails(42)
        val offline = TmdbMovieRepository(FakeTmdbApi { throw IOException("offline") }).movieDetails(42)

        assertEquals(AppResult.Failure(AppError.InvalidData), missingId)
        assertEquals(AppResult.Failure(AppError.NoConnection), offline)
    }

    private class FakeTmdbApi(
        private val details: suspend (Long) -> MovieDetailsDto,
    ) : TmdbApi {
        val detailRequests = mutableListOf<Long>()

        override suspend fun movieDetails(movieId: Long, language: String): MovieDetailsDto {
            detailRequests += movieId
            return details(movieId)
        }

        override suspend fun discoverMovies(
            page: Int,
            language: String,
            region: String,
            includeAdult: Boolean,
            sortBy: String?,
            genreId: Long?,
            minimumRating: Double?,
            releaseYear: Int?,
        ): PagedResponseDto<MovieDto> = error("Discover is not used by details tests")
    }
}
