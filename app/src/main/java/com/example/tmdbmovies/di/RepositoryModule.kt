package com.example.tmdbmovies.di

import com.example.tmdbmovies.data.repository.TmdbMovieRepository
import com.example.tmdbmovies.domain.repository.MovieRepository
import org.koin.dsl.module

internal val repositoryModule = module {
    single<MovieRepository> { TmdbMovieRepository(get()) }
}
