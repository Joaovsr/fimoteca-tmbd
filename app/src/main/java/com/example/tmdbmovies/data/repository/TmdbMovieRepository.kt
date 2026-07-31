package com.example.tmdbmovies.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.tmdbmovies.core.common.AppError
import com.example.tmdbmovies.core.common.AppResult
import com.example.tmdbmovies.core.network.toAppError
import com.example.tmdbmovies.data.remote.DiscoverMoviesPagingSource
import com.example.tmdbmovies.data.remote.SearchMoviesPagingSource
import com.example.tmdbmovies.data.remote.TmdbApi
import com.example.tmdbmovies.data.remote.mapper.toDomain
import com.example.tmdbmovies.domain.model.Movie
import com.example.tmdbmovies.domain.model.MovieDetails
import com.example.tmdbmovies.domain.model.MovieFilters
import com.example.tmdbmovies.domain.model.Genre
import com.example.tmdbmovies.domain.model.compatibleWithQuery
import com.example.tmdbmovies.domain.repository.MovieRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

internal class TmdbMovieRepository(
    private val api: TmdbApi,
) : MovieRepository {
    override fun pagedMovies(query: String, filters: MovieFilters): Flow<PagingData<Movie>> {
        val normalizedQuery = query.trim()
        val compatibleFilters = filters.compatibleWithQuery(normalizedQuery)

        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, initialLoadSize = PAGE_SIZE, enablePlaceholders = false),
            pagingSourceFactory = {
                if (normalizedQuery.isBlank()) {
                    DiscoverMoviesPagingSource(api, compatibleFilters)
                } else {
                    SearchMoviesPagingSource(
                        api = api,
                        query = normalizedQuery,
                        releaseYear = compatibleFilters.releaseYear,
                    )
                }
            },
        ).flow
    }

    override suspend fun movieDetails(movieId: Long): AppResult<MovieDetails> =
        try {
            api.movieDetails(movieId).toDomain()?.let { AppResult.Success(it) }
                ?: AppResult.Failure(AppError.InvalidData)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (exception: Exception) {
            AppResult.Failure(exception.toAppError())
        }

    override suspend fun genres(): AppResult<List<Genre>> =
        try {
            AppResult.Success(api.genres().toDomain())
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (exception: Exception) {
            AppResult.Failure(exception.toAppError())
        }

    private companion object {
        const val PAGE_SIZE = 20
    }
}
