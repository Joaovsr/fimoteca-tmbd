package com.example.tmdbmovies.feature.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tmdbmovies.domain.model.Movie
import com.example.tmdbmovies.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val repository: FavoriteRepository,
) : ViewModel() {
    val state = repository.observeFavorites()
        .map { movies -> FavoritesUiState(movies.map(Movie::toUiModel), isLoading = false) }
        .onStart { emit(FavoritesUiState(isLoading = true)) }
        .catch { emit(FavoritesUiState(isLoading = false, hasError = true)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FavoritesUiState(),
        )

    fun removeFavorite(movieId: Long) {
        val movie = state.value.movies.firstOrNull { it.id == movieId } ?: return
        viewModelScope.launch {
            repository.setFavorite(movie.toDomain(), false)
        }
    }
}

private fun Movie.toUiModel() = FavoriteMovieUiModel(
    id = movieId,
    title = title.trim(),
    releaseDate = releaseDate?.trim()?.takeIf(String::isNotEmpty),
    posterPath = posterPath,
)

private fun FavoriteMovieUiModel.toDomain() = Movie(
    movieId = id,
    title = title,
    overview = null,
    posterPath = posterPath,
    backdropPath = null,
    releaseDate = releaseDate,
    voteAverage = null,
    genreIds = emptyList(),
)
