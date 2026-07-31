package com.example.tmdbmovies.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.content.Context
import com.example.tmdbmovies.core.database.TmdbMoviesDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavoriteMovieDaoTest {
    private lateinit var database: TmdbMoviesDatabase
    private lateinit var dao: FavoriteMovieDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TmdbMoviesDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = database.favoriteMovieDao()
    }

    @After fun tearDown() = database.close()

    @Test
    fun upsertDeleteOrderingAndObservationAreStable() = runTest {
        dao.upsert(entity(1, 10))
        dao.upsert(entity(2, 20))
        dao.upsert(entity(1, 30))

        assertEquals(listOf(2L, 1L), dao.observeAll().first().map { it.movieId })
        assertEquals(10L, dao.favoriteTimestamp(1))
        assertTrue(dao.observeExists(1).first())

        dao.deleteById(1)
        dao.deleteById(1)

        assertFalse(dao.observeExists(1).first())
        assertEquals(listOf(2L), dao.observeAll().first().map { it.movieId })
    }

    @Test
    fun favoriteSurvivesClosingAndReopeningDiskDatabase() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val databaseName = "favorite-persistence-test.db"
        context.deleteDatabase(databaseName)
        try {
            val diskDatabase = Room.databaseBuilder(context, TmdbMoviesDatabase::class.java, databaseName).build()
            try {
                diskDatabase.favoriteMovieDao().upsert(entity(42, 100))
            } finally {
                diskDatabase.close()
            }

            val reopenedDatabase = Room.databaseBuilder(context, TmdbMoviesDatabase::class.java, databaseName).build()
            try {
                assertTrue(reopenedDatabase.favoriteMovieDao().observeExists(42).first())
            } finally {
                reopenedDatabase.close()
            }
        } finally {
            context.deleteDatabase(databaseName)
        }
    }

    private fun entity(id: Long, favoritedAt: Long) = FavoriteMovieEntity(
        movieId = id,
        title = "Movie $id",
        overview = null,
        posterPath = null,
        backdropPath = null,
        releaseDate = null,
        voteAverage = null,
        genreIds = listOf(18),
        favoritedAt = favoritedAt,
    )
}
