package com.example.tmdbmovies.data.remote

import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.tmdbmovies.core.common.AppError
import com.example.tmdbmovies.core.common.AppErrorException
import com.example.tmdbmovies.data.remote.dto.MovieDto
import com.example.tmdbmovies.data.remote.dto.PagedResponseDto
import com.example.tmdbmovies.domain.model.Movie
import com.example.tmdbmovies.domain.model.MovieFilters
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DiscoverMoviesPagingSourceTest {
    @Test
    fun `loads multiple TMDB pages with sequential keys and discover filters`() = runTest {
        val api =
            FakeTmdbApi { page ->
                when (page) {
                    1 -> response(page = 1, totalPages = 3, movieId = 10)
                    2 -> response(page = 2, totalPages = 3, movieId = 20)
                    else -> error("Unexpected page $page")
                }
            }
        val source =
            DiscoverMoviesPagingSource(
                api,
                MovieFilters(genreId = 18, minimumRating = 7.0, releaseYear = 2024),
            )

        val first = source.load(PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false))
        val second = source.load(PagingSource.LoadParams.Append(key = 2, loadSize = 20, placeholdersEnabled = false))

        assertPage(first, expectedMovieId = 10, expectedPrevKey = null, expectedNextKey = 2)
        assertPage(second, expectedMovieId = 20, expectedPrevKey = 1, expectedNextKey = 3)
        assertEquals(listOf(1, 2), api.requests.map { it.page })
        assertTrue(api.requests.all { it.sortBy == "popularity.desc" })
        assertTrue(api.requests.all { it.genreId == 18L && it.minimumRating == 7.0 && it.releaseYear == 2024 })
        assertTrue(api.requests.all { it.language == "pt-BR" && it.region == "BR" && !it.includeAdult })
    }

    @Test
    fun `empty results end pagination`() = runTest {
        val source = DiscoverMoviesPagingSource(FakeTmdbApi { response(page = 1, totalPages = 9) }, MovieFilters())

        val result = source.load(PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false))

        assertPage(result, expectedMovieId = null, expectedPrevKey = null, expectedNextKey = null)
    }

    @Test
    fun `last TMDB page has no next key`() = runTest {
        val source = DiscoverMoviesPagingSource(FakeTmdbApi { response(page = 3, totalPages = 3, movieId = 30) }, MovieFilters())

        val result = source.load(PagingSource.LoadParams.Append(key = 3, loadSize = 20, placeholdersEnabled = false))

        assertPage(result, expectedMovieId = 30, expectedPrevKey = 2, expectedNextKey = null)
    }

    @Test
    fun `append error is typed and does not alter already loaded page`() = runTest {
        val source =
            DiscoverMoviesPagingSource(
                FakeTmdbApi { page ->
                    if (page == 1) response(page = 1, totalPages = 2, movieId = 10) else throw IOException("offline")
                },
                MovieFilters(),
            )

        val first = source.load(PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false))
        val append = source.load(PagingSource.LoadParams.Append(key = 2, loadSize = 20, placeholdersEnabled = false))

        assertPage(first, expectedMovieId = 10, expectedPrevKey = null, expectedNextKey = 2)
        val error = (append as PagingSource.LoadResult.Error).throwable
        assertTrue(error is AppErrorException)
        assertEquals(AppError.NoConnection, (error as AppErrorException).appError)
        assertTrue(error.cause is IOException)
    }

    @Test
    fun `cancellation is rethrown instead of becoming a paging error`() = runTest {
        val source =
            DiscoverMoviesPagingSource(
                FakeTmdbApi { throw CancellationException("Superseded request") },
                MovieFilters(),
            )

        var cancellationWasRethrown = false
        try {
            source.load(PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false))
        } catch (_: CancellationException) {
            cancellationWasRethrown = true
        }
        assertTrue(cancellationWasRethrown)
    }

    @Test
    fun `refresh key is derived from the anchor page`() {
        val source = DiscoverMoviesPagingSource(FakeTmdbApi { error("Not called") }, MovieFilters())
        val state =
            PagingState(
                pages =
                    listOf(
                        PagingSource.LoadResult.Page(
                            data = listOf(movie(10)),
                            prevKey = 1,
                            nextKey = 3,
                        ),
                    ),
                anchorPosition = 0,
                config = PagingConfig(pageSize = 20),
                leadingPlaceholderCount = 0,
            )

        assertEquals(2, source.getRefreshKey(state))
    }

    private fun assertPage(
        result: PagingSource.LoadResult<Int, Movie>,
        expectedMovieId: Long?,
        expectedPrevKey: Int?,
        expectedNextKey: Int?,
    ) {
        val page = result as PagingSource.LoadResult.Page
        assertEquals(expectedPrevKey, page.prevKey)
        assertEquals(expectedNextKey, page.nextKey)
        if (expectedMovieId == null) {
            assertTrue(page.data.isEmpty())
        } else {
            assertEquals(expectedMovieId, page.data.single().movieId)
        }
    }

    private data class DiscoverRequest(
        val page: Int,
        val language: String,
        val region: String,
        val includeAdult: Boolean,
        val sortBy: String?,
        val genreId: Long?,
        val minimumRating: Double?,
        val releaseYear: Int?,
    )

    private class FakeTmdbApi(
        private val responseForPage: suspend (Int) -> PagedResponseDto<MovieDto>,
    ) : TmdbApi {
        val requests = mutableListOf<DiscoverRequest>()

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
            requests +=
                DiscoverRequest(
                    page = page,
                    language = language,
                    region = region,
                    includeAdult = includeAdult,
                    sortBy = sortBy,
                    genreId = genreId,
                    minimumRating = minimumRating,
                    releaseYear = releaseYear,
                )
            return responseForPage(page)
        }
    }

    private companion object {
        fun response(page: Int, totalPages: Int, movieId: Long? = null): PagedResponseDto<MovieDto> =
            PagedResponseDto(
                page = page,
                results = movieId?.let { listOf(MovieDto(id = it, title = "Movie $it")) }.orEmpty(),
                totalPages = totalPages,
                totalResults = totalPages,
            )

        fun movie(movieId: Long): Movie =
            Movie(movieId, "Movie $movieId", null, null, null, null, null, emptyList())
    }
}
