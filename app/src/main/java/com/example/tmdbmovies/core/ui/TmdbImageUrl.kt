package com.example.tmdbmovies.core.ui

private const val ImageBaseUrl = "https://image.tmdb.org/t/p/w500"

fun tmdbPosterUrl(path: String?): String? = path
    ?.trim()
    ?.takeIf { it.isNotEmpty() }
    ?.let { "$ImageBaseUrl/${it.removePrefix("/")}" }
