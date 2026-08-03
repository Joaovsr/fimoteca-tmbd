package com.example.tmdbmovies.di

import com.example.tmdbmovies.feature.profile.ProfileViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

internal val profileModule = module {
    viewModel { ProfileViewModel(get()) }
}
