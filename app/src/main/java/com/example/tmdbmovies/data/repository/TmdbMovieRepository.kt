package com.example.tmdbmovies.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.tmdbmovies.data.remote.DiscoverMoviesPagingSource
import com.example.tmdbmovies.data.remote.TmdbApi
import com.example.tmdbmovies.domain.model.Movie
import com.example.tmdbmovies.domain.model.MovieFilters
import com.example.tmdbmovies.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow

internal class TmdbMovieRepository(
    private val api: TmdbApi,
) : MovieRepository {
    override fun pagedMovies(query: String, filters: MovieFilters): Flow<PagingData<Movie>> {
        require(query.isBlank()) { "Movie search is not available yet." }

        return Pager(
            config = PagingConfig(pageSize = PAGE_SIZE, initialLoadSize = PAGE_SIZE, enablePlaceholders = false),
            pagingSourceFactory = { DiscoverMoviesPagingSource(api, filters) },
        ).flow
    }

    private companion object {
        const val PAGE_SIZE = 20
    }
}
