package com.example.tmdbmovies.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.tmdbmovies.data.local.FavoriteMovieDao
import com.example.tmdbmovies.data.local.FavoriteMovieEntity
import com.example.tmdbmovies.data.local.RoomConverters

@Database(entities = [FavoriteMovieEntity::class], version = 1, exportSchema = false)
@TypeConverters(RoomConverters::class)
abstract class TmdbMoviesDatabase : RoomDatabase() {
    abstract fun favoriteMovieDao(): FavoriteMovieDao
}
