package com.example.tmdbmovies.domain.repository

import androidx.paging.PagingData
import com.example.tmdbmovies.core.common.AppResult
import com.example.tmdbmovies.domain.model.Genre
import com.example.tmdbmovies.domain.model.Movie
import com.example.tmdbmovies.domain.model.MovieCollection
import com.example.tmdbmovies.domain.model.MovieDetails
import com.example.tmdbmovies.domain.model.MovieFilters
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    fun pagedMovies(query: String, filters: MovieFilters): Flow<PagingData<Movie>>

    suspend fun movies(collection: MovieCollection): AppResult<List<Movie>>

    suspend fun movieDetails(movieId: Long): AppResult<MovieDetails>

    suspend fun genres(): AppResult<List<Genre>>
}
