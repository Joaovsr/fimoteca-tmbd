package com.example.tmdbmovies.domain.repository

import com.example.tmdbmovies.core.common.AppResult
import com.example.tmdbmovies.domain.model.Movie
import kotlinx.coroutines.flow.Flow

interface FavoriteRepository {
    fun observeFavorites(): Flow<List<Movie>>

    fun observeFavorite(movieId: Long): Flow<Boolean>

    suspend fun setFavorite(movie: Movie, favorite: Boolean): AppResult<Unit>
}
