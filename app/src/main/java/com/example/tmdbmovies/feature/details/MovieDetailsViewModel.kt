package com.example.tmdbmovies.feature.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tmdbmovies.R
import com.example.tmdbmovies.core.common.AppError
import com.example.tmdbmovies.core.common.AppResult
import com.example.tmdbmovies.domain.model.MovieDetails
import com.example.tmdbmovies.domain.repository.MovieRepository
import com.example.tmdbmovies.domain.repository.FavoriteRepository
import com.example.tmdbmovies.domain.model.Movie
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MovieDetailsViewModel(
    private val repository: MovieRepository,
    private val favoriteRepository: FavoriteRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val movieId: Long = checkNotNull(savedStateHandle["movieId"])
    private val mutableState = MutableLiveData<MovieDetailsUiState>(MovieDetailsUiState.Loading)
    private var details: MovieDetails? = null
    private var isFavorite = false

    val state: LiveData<MovieDetailsUiState> = mutableState

    init {
        observeFavorite()
        loadDetails()
    }

    fun retry() {
        loadDetails()
    }

    fun onFavoriteClick() {
        val movie = details?.toFavoriteMovie() ?: return
        viewModelScope.launch {
            favoriteRepository.setFavorite(movie, !isFavorite)
        }
    }

    private fun observeFavorite() {
        viewModelScope.launch {
            favoriteRepository.observeFavorite(movieId).collectLatest { favorite ->
                isFavorite = favorite
                val content = mutableState.value as? MovieDetailsUiState.Content
                if (content != null) mutableState.value = content.copy(isFavorite = favorite)
            }
        }
    }

    private fun loadDetails() {
        mutableState.value = MovieDetailsUiState.Loading
        viewModelScope.launch {
            mutableState.value = when (val result = repository.movieDetails(movieId)) {
                is AppResult.Success -> {
                    details = result.value
                    MovieDetailsUiState.Content(result.value.toUiModel(), isFavorite)
                }
                is AppResult.Failure -> MovieDetailsUiState.Error(result.error.messageRes())
            }
        }
    }
}

private fun MovieDetails.toUiModel() = MovieDetailsUiModel(
    id = movieId,
    title = title.trim().takeIf(String::isNotEmpty),
    overview = overview?.trim()?.takeIf(String::isNotEmpty),
    releaseDate = releaseDate?.trim()?.takeIf(String::isNotEmpty),
    posterPath = posterPath?.trim()?.takeIf(String::isNotEmpty),
)

private fun MovieDetails.toFavoriteMovie() = Movie(
    movieId = movieId,
    title = title,
    overview = overview,
    posterPath = posterPath,
    backdropPath = backdropPath,
    releaseDate = releaseDate,
    voteAverage = null,
    genreIds = emptyList(),
)

private fun AppError.messageRes(): Int = when (this) {
    AppError.NoConnection -> R.string.error_no_connection
    AppError.Timeout -> R.string.error_timeout
    AppError.Unauthorized -> R.string.error_unauthorized
    AppError.RateLimited -> R.string.error_rate_limited
    AppError.NotFound -> R.string.details_error_not_found
    is AppError.UnexpectedHttp,
    AppError.InvalidData,
    AppError.Unknown,
    -> R.string.details_error_generic
}
