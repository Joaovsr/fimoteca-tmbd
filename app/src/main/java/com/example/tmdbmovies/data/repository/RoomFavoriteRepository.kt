package com.example.tmdbmovies.data.repository

import com.example.tmdbmovies.core.common.AppError
import com.example.tmdbmovies.core.common.AppResult
import com.example.tmdbmovies.data.local.FavoriteMovieDao
import com.example.tmdbmovies.data.local.toDomain
import com.example.tmdbmovies.data.local.toEntity
import com.example.tmdbmovies.domain.model.Movie
import com.example.tmdbmovies.domain.repository.FavoriteRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class RoomFavoriteRepository(
    private val dao: FavoriteMovieDao,
    private val now: () -> Long = System::currentTimeMillis,
) : FavoriteRepository {
    override fun observeFavorites(): Flow<List<Movie>> =
        dao.observeAll().map { movies -> movies.map { it.toDomain() } }

    override fun observeFavorite(movieId: Long): Flow<Boolean> = dao.observeExists(movieId)

    override suspend fun setFavorite(movie: Movie, favorite: Boolean): AppResult<Unit> =
        try {
            if (favorite) dao.upsert(movie.toEntity(now())) else dao.deleteById(movie.movieId)
            AppResult.Success(Unit)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            AppResult.Failure(AppError.Unknown)
        }
}
