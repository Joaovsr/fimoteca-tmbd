package com.example.tmdbmovies.di

import com.example.tmdbmovies.feature.discover.DiscoverViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val discoverModule = module {
    viewModel { DiscoverViewModel(get(), get(), get()) }
}
