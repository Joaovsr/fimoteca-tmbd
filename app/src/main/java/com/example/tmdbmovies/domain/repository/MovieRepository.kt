package com.example.tmdbmovies.domain.repository

import androidx.paging.PagingData
import com.example.tmdbmovies.domain.model.Movie
import com.example.tmdbmovies.domain.model.MovieFilters
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    fun pagedMovies(query: String, filters: MovieFilters): Flow<PagingData<Movie>>
}
