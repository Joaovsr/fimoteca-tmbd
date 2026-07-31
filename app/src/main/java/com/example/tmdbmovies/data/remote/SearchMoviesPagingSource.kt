package com.example.tmdbmovies.data.remote

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.tmdbmovies.core.common.AppErrorException
import com.example.tmdbmovies.core.network.toAppError
import com.example.tmdbmovies.data.remote.mapper.toRemoteMoviePage
import com.example.tmdbmovies.domain.model.Movie
import kotlinx.coroutines.CancellationException

internal class SearchMoviesPagingSource(
    private val api: TmdbApi,
    private val query: String,
    private val releaseYear: Int?,
) : PagingSource<Int, Movie>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> {
        val requestedPage = params.key ?: FIRST_PAGE

        return try {
            val response =
                api.searchMovies(
                    query = query,
                    page = requestedPage,
                    releaseYear = releaseYear,
                ).toRemoteMoviePage()

            LoadResult.Page(
                data = response.movies,
                prevKey = (response.page - 1).takeIf { response.page > FIRST_PAGE },
                nextKey =
                    (response.page + 1).takeUnless {
                        response.movies.isEmpty() || response.page >= response.totalPages
                    },
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            LoadResult.Error(AppErrorException(exception.toAppError(), exception))
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Movie>): Int? =
        state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.let { closestPage ->
                closestPage.prevKey?.plus(1) ?: closestPage.nextKey?.minus(1)
            }
        }

    private companion object {
        const val FIRST_PAGE = 1
    }
}
