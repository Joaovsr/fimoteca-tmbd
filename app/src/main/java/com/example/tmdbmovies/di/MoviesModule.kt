package com.example.tmdbmovies.di

import com.example.tmdbmovies.feature.movies.MoviesViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val moviesModule = module {
    viewModel { MoviesViewModel(get()) }
}
