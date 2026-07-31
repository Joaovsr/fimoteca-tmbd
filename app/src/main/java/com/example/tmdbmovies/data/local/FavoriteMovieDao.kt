package com.example.tmdbmovies.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteMovieDao {
    @Query("SELECT * FROM favorite_movies ORDER BY favoritedAt DESC, movieId DESC")
    fun observeAll(): Flow<List<FavoriteMovieEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_movies WHERE movieId = :movieId)")
    fun observeExists(movieId: Long): Flow<Boolean>

    @Query("SELECT favoritedAt FROM favorite_movies WHERE movieId = :movieId")
    suspend fun favoriteTimestamp(movieId: Long): Long?

    @Upsert
    suspend fun upsertReplacing(movie: FavoriteMovieEntity)

    @Transaction
    suspend fun upsert(movie: FavoriteMovieEntity) {
        val originalTimestamp = favoriteTimestamp(movie.movieId)
        upsertReplacing(movie.copy(favoritedAt = originalTimestamp ?: movie.favoritedAt))
    }

    @Query("DELETE FROM favorite_movies WHERE movieId = :movieId")
    suspend fun deleteById(movieId: Long)
}
