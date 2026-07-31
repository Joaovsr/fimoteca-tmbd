package com.example.tmdbmovies.feature.movies

import com.example.tmdbmovies.domain.model.Genre
import com.example.tmdbmovies.domain.model.MovieFilters

data class MoviesUiState(
    val query: String = "",
    val filters: MovieFilters = MovieFilters(),
    val genres: List<Genre> = emptyList(),
    val isLoadingGenres: Boolean = false,
    val genresErrorMessageRes: Int? = null,
) {
    val isSearching: Boolean
        get() = query.isNotBlank()
}
