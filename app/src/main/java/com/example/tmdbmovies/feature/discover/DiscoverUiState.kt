package com.example.tmdbmovies.feature.discover

import androidx.annotation.StringRes
import com.example.tmdbmovies.domain.model.MovieCollection
import com.example.tmdbmovies.feature.movies.MovieUiModel

data class DiscoverSectionState(
    val movies: List<MovieUiModel> = emptyList(),
    val isLoading: Boolean = true,
    @param:StringRes val errorMessageRes: Int? = null,
)

data class DiscoverUiState(
    val sections: Map<MovieCollection, DiscoverSectionState> = MovieCollection.entries.associateWith {
        DiscoverSectionState()
    },
    val selectedFeaturedIndex: Int = 0,
) {
    val featuredMovies: List<MovieUiModel>
        get() = sections[MovieCollection.TrendingWeekly]?.movies.orEmpty().take(5)

    val featuredMovie: MovieUiModel?
        get() = featuredMovies.getOrNull(selectedFeaturedIndex.coerceIn(0, (featuredMovies.size - 1).coerceAtLeast(0)))
}
