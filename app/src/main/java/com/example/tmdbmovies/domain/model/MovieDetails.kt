package com.example.tmdbmovies.domain.model

data class MovieDetails(
    val movieId: Long,
    val title: String,
    val overview: String?,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String?,
)
