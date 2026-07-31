package com.example.tmdbmovies.data.remote.mapper

import com.example.tmdbmovies.data.remote.RemoteMoviePage
import com.example.tmdbmovies.data.remote.dto.MovieDto
import com.example.tmdbmovies.data.remote.dto.PagedResponseDto
import com.example.tmdbmovies.domain.model.Movie

internal fun PagedResponseDto<MovieDto>.toRemoteMoviePage(): RemoteMoviePage =
    RemoteMoviePage(
        page = page,
        movies = results.mapNotNull(MovieDto::toDomain),
        totalPages = totalPages,
        totalResults = totalResults,
    )

private fun MovieDto.toDomain(): Movie? {
    val movieId = id ?: return null

    return Movie(
        movieId = movieId,
        title = title.orEmpty().trim(),
        overview = overview.normalizedText(),
        posterPath = posterPath.normalizedPath(),
        backdropPath = backdropPath.normalizedPath(),
        releaseDate = releaseDate.normalizedDate(),
        voteAverage = voteAverage?.takeIf { it.isFinite() },
        genreIds = genreIds,
    )
}

private fun String?.normalizedText(): String? = this?.trim()?.takeIf(String::isNotEmpty)

private fun String?.normalizedPath(): String? = normalizedText()?.let { path ->
    if (path.startsWith('/')) path else "/$path"
}

private fun String?.normalizedDate(): String? =
    normalizedText()?.takeIf(ISO_DATE_REGEX::matches)

private val ISO_DATE_REGEX = Regex("""\d{4}-(0[1-9]|1[0-2])-([0-2]\d|3[01])""")
