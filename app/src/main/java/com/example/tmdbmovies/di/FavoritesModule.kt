package com.example.tmdbmovies.di

import com.example.tmdbmovies.feature.favorites.FavoritesViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

internal val favoritesModule = module {
    viewModel { FavoritesViewModel(get(), get()) }
}
