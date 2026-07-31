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
import kotlinx.coroutines.launch

class MovieDetailsViewModel(
    private val repository: MovieRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val movieId: Long = checkNotNull(savedStateHandle["movieId"])
    private val mutableState = MutableLiveData<MovieDetailsUiState>(MovieDetailsUiState.Loading)

    val state: LiveData<MovieDetailsUiState> = mutableState

    init {
        loadDetails()
    }

    fun retry() {
        loadDetails()
    }

    private fun loadDetails() {
        mutableState.value = MovieDetailsUiState.Loading
        viewModelScope.launch {
            mutableState.value = when (val result = repository.movieDetails(movieId)) {
                is AppResult.Success -> MovieDetailsUiState.Content(result.value.toUiModel())
                is AppResult.Failure -> MovieDetailsUiState.Error(result.error.messageRes())
            }
        }
    }
}

private fun MovieDetails.toUiModel() = MovieDetailsUiModel(
    title = title.trim().takeIf(String::isNotEmpty),
    overview = overview?.trim()?.takeIf(String::isNotEmpty),
    releaseDate = releaseDate?.trim()?.takeIf(String::isNotEmpty),
    posterPath = posterPath?.trim()?.takeIf(String::isNotEmpty),
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
