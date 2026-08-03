package com.example.tmdbmovies.feature.discover

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.annotation.StringRes
import com.example.tmdbmovies.R
import com.example.tmdbmovies.core.common.AppError
import com.example.tmdbmovies.core.common.AppResult
import com.example.tmdbmovies.domain.model.MovieCollection
import com.example.tmdbmovies.domain.repository.FavoriteRepository
import com.example.tmdbmovies.domain.repository.MovieRepository
import com.example.tmdbmovies.feature.movies.MovieUiModel
import com.example.tmdbmovies.feature.movies.toDomain
import com.example.tmdbmovies.feature.movies.toUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DiscoverViewModel(
    private val movieRepository: MovieRepository,
    private val favoriteRepository: FavoriteRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val remoteMovies = MovieCollection.entries.associateWith { emptyList<MovieUiModel>() }.toMutableMap()
    private var favoriteIds = emptySet<Long>()
    private val _state = MutableStateFlow(
        DiscoverUiState(selectedFeaturedIndex = savedStateHandle[FEATURED_INDEX_KEY] ?: 0),
    )
    val state: StateFlow<DiscoverUiState> = _state.asStateFlow()

    init {
        observeFavorites()
        MovieCollection.entries.forEach(::load)
    }

    fun retry(collection: MovieCollection) = load(collection)

    fun selectFeatured(index: Int) {
        if (index !in state.value.featuredMovies.indices) return
        savedStateHandle[FEATURED_INDEX_KEY] = index
        _state.update { it.copy(selectedFeaturedIndex = index) }
    }

    fun onFavoriteClick(movie: MovieUiModel) {
        viewModelScope.launch {
            favoriteRepository.setFavorite(movie.toDomain(), !movie.isFavorite)
        }
    }

    private fun load(collection: MovieCollection) {
        _state.update { current ->
            current.copy(sections = current.sections + (collection to current.section(collection).copy(
                isLoading = true,
                errorMessageRes = null,
            )))
        }
        viewModelScope.launch {
            when (val result = movieRepository.movies(collection)) {
                is AppResult.Success -> {
                    remoteMovies[collection] = result.value.map { it.toUiModel(it.movieId in favoriteIds) }
                    updateSection(collection, movies = remoteMovies.getValue(collection))
                }
                is AppResult.Failure -> updateSection(collection, errorMessageRes = result.error.messageRes())
            }
        }
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            favoriteRepository.observeFavorites().collectLatest { favorites ->
                favoriteIds = favorites.mapTo(mutableSetOf()) { it.movieId }
                remoteMovies.keys.forEach { collection ->
                    remoteMovies[collection] = remoteMovies.getValue(collection).map {
                        it.copy(isFavorite = it.id in favoriteIds)
                    }
                }
                _state.update { current ->
                    current.copy(sections = current.sections.mapValues { (collection, section) ->
                        section.copy(movies = remoteMovies.getValue(collection))
                    })
                }
            }
        }
    }

    private fun updateSection(
        collection: MovieCollection,
        movies: List<MovieUiModel> = emptyList(),
        @StringRes errorMessageRes: Int? = null,
    ) {
        _state.update { current ->
            val selectedIndex = if (collection == MovieCollection.TrendingWeekly && movies.isNotEmpty()) {
                current.selectedFeaturedIndex.coerceIn(0, movies.take(5).lastIndex)
            } else current.selectedFeaturedIndex
            current.copy(
                sections = current.sections + (collection to DiscoverSectionState(
                    movies = movies,
                    isLoading = false,
                    errorMessageRes = errorMessageRes,
                )),
                selectedFeaturedIndex = selectedIndex,
            )
        }
    }

    private fun DiscoverUiState.section(collection: MovieCollection) =
        sections[collection] ?: DiscoverSectionState()

    private companion object {
        const val FEATURED_INDEX_KEY = "discover.featured.index"
    }
}

private fun AppError.messageRes(): Int = when (this) {
    AppError.NoConnection -> R.string.error_no_connection
    AppError.Timeout -> R.string.error_timeout
    AppError.Unauthorized -> R.string.error_unauthorized
    AppError.RateLimited -> R.string.error_rate_limited
    else -> R.string.error_generic
}
