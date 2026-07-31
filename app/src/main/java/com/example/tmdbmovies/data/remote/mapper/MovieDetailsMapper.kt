package com.example.tmdbmovies.data.remote.mapper

import com.example.tmdbmovies.data.remote.dto.MovieDetailsDto
import com.example.tmdbmovies.domain.model.MovieDetails

internal fun MovieDetailsDto.toDomain(): MovieDetails? {
    val movieId = id ?: return null

    return MovieDetails(
        movieId = movieId,
        title = title.orEmpty().trim(),
        overview = overview.normalizedText(),
        posterPath = posterPath.normalizedPath(),
        backdropPath = backdropPath.normalizedPath(),
        releaseDate = releaseDate.normalizedDate(),
    )
}

private fun String?.normalizedText(): String? = this?.trim()?.takeIf(String::isNotEmpty)

private fun String?.normalizedPath(): String? = normalizedText()?.let { path ->
    if (path.startsWith('/')) path else "/$path"
}

private fun String?.normalizedDate(): String? =
    normalizedText()?.takeIf(ISO_DATE_REGEX::matches)

private val ISO_DATE_REGEX = Regex("""\d{4}-(0[1-9]|1[0-2])-([0-2]\d|3[01])""")
