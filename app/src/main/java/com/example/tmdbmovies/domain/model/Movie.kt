package com.example.tmdbmovies.domain.model

data class Movie(
    val movieId: Long,
    val title: String,
    val overview: String?,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String?,
    val voteAverage: Double?,
    val genreIds: List<Long>,
)
