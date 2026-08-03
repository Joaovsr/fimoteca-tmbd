package com.example.tmdbmovies.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tmdbmovies.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ProfileViewModel(repository: FavoriteRepository) : ViewModel() {
    val favoriteCount: StateFlow<Int> = repository.observeFavorites()
        .map { favorites -> favorites.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
}
