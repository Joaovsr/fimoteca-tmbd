package com.example.tmdbmovies.feature.favorites

data class FavoritesUiState(
    val movies: List<FavoriteMovieUiModel> = emptyList(),
    val totalCount: Int = 0,
    val query: String = "",
    val sortOrder: FavoriteSortOrder = FavoriteSortOrder.RecentlyAdded,
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
)

enum class FavoriteSortOrder {
    RecentlyAdded,
    TitleAscending,
    RatingDescending,
    ReleaseDateDescending,
    ReleaseDateAscending,
}

data class FavoriteMovieUiModel(
    val id: Long,
    val title: String,
    val releaseDate: String?,
    val posterPath: String?,
    val voteAverage: Double? = null,
)
