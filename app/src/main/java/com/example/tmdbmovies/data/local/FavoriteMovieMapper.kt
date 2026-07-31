package com.example.tmdbmovies.data.local

import com.example.tmdbmovies.domain.model.Movie

internal fun Movie.toEntity(favoritedAt: Long) = FavoriteMovieEntity(
    movieId = movieId,
    title = title,
    overview = overview,
    posterPath = posterPath,
    backdropPath = backdropPath,
    releaseDate = releaseDate,
    voteAverage = voteAverage,
    genreIds = genreIds,
    favoritedAt = favoritedAt,
)

internal fun FavoriteMovieEntity.toDomain() = Movie(
    movieId = movieId,
    title = title,
    overview = overview,
    posterPath = posterPath,
    backdropPath = backdropPath,
    releaseDate = releaseDate,
    voteAverage = voteAverage,
    genreIds = genreIds,
)
