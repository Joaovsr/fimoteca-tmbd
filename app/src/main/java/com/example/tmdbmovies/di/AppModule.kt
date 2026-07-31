package com.example.tmdbmovies.di

import org.koin.dsl.module

val appModule = module {
    includes(networkModule, databaseModule, repositoryModule, moviesModule, detailsModule, favoritesModule)
}
