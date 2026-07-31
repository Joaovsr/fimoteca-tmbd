package com.example.tmdbmovies.data.remote

import androidx.paging.PagingSource
import com.example.tmdbmovies.core.common.AppError
import com.example.tmdbmovies.core.common.AppErrorException
import com.example.tmdbmovies.data.remote.dto.GenreListDto
import com.example.tmdbmovies.data.remote.dto.MovieDetailsDto
import com.example.tmdbmovies.data.remote.dto.MovieDto
import com.example.tmdbmovies.data.remote.dto.PagedResponseDto
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SearchMoviesPagingSourceTest {
    @Test
    fun `search pages use normalized query snapshot and compatible year`() = runTest {
        val api = FakeTmdbApi()
        val source = SearchMoviesPagingSource(api, query = "Alien", releaseYear = 1979)

        val first = source.load(PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false))
        val second = source.load(PagingSource.LoadParams.Append(key = 2, loadSize = 20, placeholdersEnabled = false))

        assertPage(first, expectedMovieId = 1, expectedPrevKey = null, expectedNextKey = 2)
        assertPage(second, expectedMovieId = 2, expectedPrevKey = 1, expectedNextKey = null)
        assertEquals(listOf(SearchRequest("Alien", 1, 1979), SearchRequest("Alien", 2, 1979)), api.requests)
    }

    @Test
    fun `search failure is typed and cancellation is rethrown`() = runTest {
        val offline = SearchMoviesPagingSource(FakeTmdbApi { throw IOException("offline") }, "Alien", null)
        val error = offline.load(PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false))
        assertEquals(AppError.NoConnection, ((error as PagingSource.LoadResult.Error).throwable as AppErrorException).appError)

        val cancelled = SearchMoviesPagingSource(FakeTmdbApi { throw CancellationException("cancelled") }, "Alien", null)
        var cancellationWasRethrown = false
        try {
            cancelled.load(PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false))
        } catch (_: CancellationException) {
            cancellationWasRethrown = true
        }
        assertTrue(cancellationWasRethrown)
    }

    private fun assertPage(
        result: PagingSource.LoadResult<Int, com.example.tmdbmovies.domain.model.Movie>,
        expectedMovieId: Long,
        expectedPrevKey: Int?,
        expectedNextKey: Int?,
    ) {
        val page = result as PagingSource.LoadResult.Page
        assertEquals(expectedMovieId, page.data.single().movieId)
        assertEquals(expectedPrevKey, page.prevKey)
        assertEquals(expectedNextKey, page.nextKey)
    }

    private class FakeTmdbApi(
        private val response: (suspend (Int) -> PagedResponseDto<MovieDto>)? = null,
    ) : TmdbApi {
        val requests = mutableListOf<SearchRequest>()

        override suspend fun searchMovies(query: String, page: Int, language: String, includeAdult: Boolean, releaseYear: Int?): PagedResponseDto<MovieDto> {
            requests += SearchRequest(query, page, releaseYear)
            return response?.invoke(page)
                ?: PagedResponseDto(page, listOf(MovieDto(id = page.toLong(), title = query)), 2, 2)
        }

        override suspend fun discoverMovies(page: Int, language: String, region: String, includeAdult: Boolean, sortBy: String?, genreId: Long?, minimumRating: Double?, releaseYear: Int?): PagedResponseDto<MovieDto> =
            error("Discover is not used by search tests")

        override suspend fun movieDetails(movieId: Long, language: String): MovieDetailsDto = error("Details are not used by search tests")

        override suspend fun genres(language: String): GenreListDto = error("Genres are not used by search tests")
    }

    private data class SearchRequest(val query: String, val page: Int, val releaseYear: Int?)
}
