package com.example.tmdbmovies.feature.details

import androidx.annotation.StringRes

sealed interface MovieDetailsUiState {
    data object Loading : MovieDetailsUiState

    data class Content(val movie: MovieDetailsUiModel) : MovieDetailsUiState

    data class Error(@param:StringRes val messageRes: Int) : MovieDetailsUiState
}

data class MovieDetailsUiModel(
    val title: String?,
    val overview: String?,
    val releaseDate: String?,
    val posterPath: String?,
)
