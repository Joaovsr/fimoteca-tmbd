package com.example.tmdbmovies.data.local

import com.example.tmdbmovies.domain.model.Movie
import org.junit.Assert.assertEquals
import org.junit.Test

class FavoriteMovieMapperTest {
    @Test
    fun `maps movie to entity and back without losing list data`() {
        val movie = Movie(7, "Title", "Overview", "/poster", "/backdrop", "2026-07-31", 8.5, listOf(18, 28))

        val entity = movie.toEntity(favoritedAt = 123)

        assertEquals(123, entity.favoritedAt)
        assertEquals(movie, entity.toDomain())
    }
}
