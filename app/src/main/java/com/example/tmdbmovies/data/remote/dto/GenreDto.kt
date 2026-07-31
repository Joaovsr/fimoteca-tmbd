package com.example.tmdbmovies.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class GenreDto(
    val id: Long? = null,
    val name: String? = null,
)

@Serializable
internal data class GenreListDto(
    val genres: List<GenreDto> = emptyList(),
)
