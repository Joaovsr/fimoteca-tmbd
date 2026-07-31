package com.example.tmdbmovies.test

import com.example.tmdbmovies.core.common.AppResult
import com.example.tmdbmovies.domain.model.Movie
import com.example.tmdbmovies.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeFavoriteRepository(
    favorites: List<Movie> = emptyList(),
) : FavoriteRepository {
    val favorites = MutableStateFlow(favorites)
    val changes = mutableListOf<Pair<Movie, Boolean>>()

    override fun observeFavorites(): Flow<List<Movie>> = favorites

    override fun observeFavorite(movieId: Long): Flow<Boolean> =
        favorites.map { movies -> movies.any { it.movieId == movieId } }

    override suspend fun setFavorite(movie: Movie, favorite: Boolean): AppResult<Unit> {
        changes += movie to favorite
        favorites.value = if (favorite) {
            listOf(movie) + favorites.value.filterNot { it.movieId == movie.movieId }
        } else {
            favorites.value.filterNot { it.movieId == movie.movieId }
        }
        return AppResult.Success(Unit)
    }
}
