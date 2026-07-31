package com.example.tmdbmovies.feature.movies

import com.example.tmdbmovies.domain.model.Movie

data class MovieUiModel(
    val id: Long,
    val title: String,
    val releaseDate: String?,
    val posterPath: String?,
)

internal fun Movie.toUiModel() = MovieUiModel(
    id = movieId,
    title = title.trim(),
    releaseDate = releaseDate?.trim()?.takeIf { it.isNotEmpty() },
    posterPath = posterPath,
)
