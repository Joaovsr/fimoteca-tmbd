package com.example.tmdbmovies.feature.favorites

data class FavoritesUiState(
    val movies: List<FavoriteMovieUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
)

data class FavoriteMovieUiModel(
    val id: Long,
    val title: String,
    val releaseDate: String?,
    val posterPath: String?,
)
