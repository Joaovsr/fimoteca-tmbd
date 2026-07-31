package com.example.tmdbmovies.di

import androidx.room.Room
import com.example.tmdbmovies.core.database.TmdbMoviesDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

internal val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            TmdbMoviesDatabase::class.java,
            "tmdb-movies.db",
        ).build()
    }
    single { get<TmdbMoviesDatabase>().favoriteMovieDao() }
}
