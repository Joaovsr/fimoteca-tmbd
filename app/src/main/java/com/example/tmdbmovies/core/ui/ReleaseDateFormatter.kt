package com.example.tmdbmovies.core.ui

private val ISO_RELEASE_DATE = Regex("""^(\d{4})-(\d{2})-(\d{2})$""")

fun formatReleaseDate(releaseDate: String?): String? {
    val match = releaseDate?.let(ISO_RELEASE_DATE::matchEntire) ?: return null
    val (year, month, day) = match.destructured
    return "$day/$month/$year"
}
