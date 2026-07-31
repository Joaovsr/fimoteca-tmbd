package com.example.tmdbmovies.data.repository

import com.example.tmdbmovies.core.common.AppResult
import com.example.tmdbmovies.data.local.FavoriteMovieDao
import com.example.tmdbmovies.data.local.FavoriteMovieEntity
import com.example.tmdbmovies.domain.model.Movie
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RoomFavoriteRepositoryTest {
    @Test
    fun `upsert and delete are idempotent and immediately observed`() = runTest {
        val dao = FakeDao()
        val repository = RoomFavoriteRepository(dao, now = { 99L })
        val movie = movie(42)

        assertTrue(repository.setFavorite(movie, true) is AppResult.Success)
        assertTrue(repository.setFavorite(movie, true) is AppResult.Success)
        assertEquals(listOf(movie), repository.observeFavorites().first())
        assertTrue(repository.observeFavorite(42).first())
        assertEquals(99L, dao.favoriteTimestamp(42))

        assertTrue(repository.setFavorite(movie, false) is AppResult.Success)
        assertTrue(repository.setFavorite(movie, false) is AppResult.Success)
        assertFalse(repository.observeFavorite(42).first())
    }

    private class FakeDao : FavoriteMovieDao {
        private val entities = MutableStateFlow<List<FavoriteMovieEntity>>(emptyList())

        override fun observeAll(): Flow<List<FavoriteMovieEntity>> = entities

        override fun observeExists(movieId: Long): Flow<Boolean> =
            entities.map { values -> values.any { it.movieId == movieId } }

        override suspend fun favoriteTimestamp(movieId: Long): Long? =
            entities.value.firstOrNull { it.movieId == movieId }?.favoritedAt

        override suspend fun upsertReplacing(movie: FavoriteMovieEntity) {
            entities.value = listOf(movie) + entities.value.filterNot { it.movieId == movie.movieId }
        }

        override suspend fun deleteById(movieId: Long) {
            entities.value = entities.value.filterNot { it.movieId == movieId }
        }
    }
}

private fun movie(id: Long) = Movie(id, "Movie $id", null, null, null, null, null, emptyList())
