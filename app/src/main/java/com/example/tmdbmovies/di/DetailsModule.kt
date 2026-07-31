package com.example.tmdbmovies.di

import com.example.tmdbmovies.feature.details.MovieDetailsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

internal val detailsModule = module {
    viewModel { MovieDetailsViewModel(get(), get(), get()) }
}
