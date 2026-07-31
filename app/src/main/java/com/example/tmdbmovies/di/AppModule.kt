package com.example.tmdbmovies.di

import org.koin.dsl.module

val appModule = module {
    includes(networkModule, repositoryModule, moviesModule, detailsModule)
}
