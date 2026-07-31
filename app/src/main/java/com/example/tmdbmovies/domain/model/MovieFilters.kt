package com.example.tmdbmovies.domain.model

data class MovieFilters(
    val genreId: Long? = null,
    val sortOrder: MovieSortOrder = MovieSortOrder.PopularityDescending,
    val minimumRating: Double? = null,
    val releaseYear: Int? = null,
)

enum class MovieSortOrder {
    PopularityDescending,
    ReleaseDateDescending,
    VoteAverageDescending,
}

fun MovieFilters.compatibleWithQuery(query: String): MovieFilters =
    if (query.isBlank()) this else copy(
        genreId = null,
        sortOrder = MovieSortOrder.PopularityDescending,
        minimumRating = null,
    )
