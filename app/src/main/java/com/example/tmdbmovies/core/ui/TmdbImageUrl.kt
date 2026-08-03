package com.example.tmdbmovies.core.ui

private const val ImageBaseUrl = "https://image.tmdb.org/t/p/w500"
private const val BackdropImageBaseUrl = "https://image.tmdb.org/t/p/w780"

fun tmdbPosterUrl(path: String?): String? = path
    ?.trim()
    ?.takeIf { it.isNotEmpty() }
    ?.let { "$ImageBaseUrl/${it.removePrefix("/")}" }

fun tmdbBackdropUrl(path: String?): String? = path
    ?.trim()
    ?.takeIf { it.isNotEmpty() }
    ?.let { "$BackdropImageBaseUrl/${it.removePrefix("/")}" }
