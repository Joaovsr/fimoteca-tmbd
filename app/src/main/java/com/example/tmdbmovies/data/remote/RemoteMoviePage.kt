package com.example.tmdbmovies.data.remote

import com.example.tmdbmovies.domain.model.Movie

internal data class RemoteMoviePage(
    val page: Int,
    val movies: List<Movie>,
    val totalPages: Int,
    val totalResults: Int,
)
