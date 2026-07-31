package com.example.tmdbmovies.domain.model

data class MovieFilters(
    val genreId: Long? = null,
    val sortOrder: MovieSortOrder = MovieSortOrder.PopularityDescending,
    val minimumRating: Double? = null,
    val releaseYear: Int? = null,
)

enum class MovieSortOrder {
    PopularityDescending,
}
