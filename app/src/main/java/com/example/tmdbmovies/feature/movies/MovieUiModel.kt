package com.example.tmdbmovies.feature.movies

import com.example.tmdbmovies.domain.model.Movie

data class MovieUiModel(
    val id: Long,
    val title: String,
    val releaseDate: String?,
    val posterPath: String?,
    val overview: String? = null,
    val backdropPath: String? = null,
    val voteAverage: Double? = null,
    val genreIds: List<Long> = emptyList(),
    val isFavorite: Boolean = false,
)

internal fun Movie.toUiModel(isFavorite: Boolean = false) = MovieUiModel(
    id = movieId,
    title = title.trim(),
    releaseDate = releaseDate?.trim()?.takeIf { it.isNotEmpty() },
    posterPath = posterPath,
    overview = overview,
    backdropPath = backdropPath,
    voteAverage = voteAverage,
    genreIds = genreIds,
    isFavorite = isFavorite,
)

internal fun MovieUiModel.toDomain() = Movie(
    movieId = id,
    title = title,
    overview = overview,
    posterPath = posterPath,
    backdropPath = backdropPath,
    releaseDate = releaseDate,
    voteAverage = voteAverage,
    genreIds = genreIds,
)
