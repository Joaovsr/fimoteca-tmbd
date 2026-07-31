package com.example.tmdbmovies.data.remote.mapper

import com.example.tmdbmovies.data.remote.dto.GenreDto
import com.example.tmdbmovies.data.remote.dto.GenreListDto
import com.example.tmdbmovies.domain.model.Genre

internal fun GenreListDto.toDomain(): List<Genre> = genres.mapNotNull(GenreDto::toDomain)

private fun GenreDto.toDomain(): Genre? {
    val genreId = id ?: return null
    val normalizedName = name?.trim()?.takeIf(String::isNotEmpty) ?: return null
    return Genre(genreId = genreId, name = normalizedName)
}
