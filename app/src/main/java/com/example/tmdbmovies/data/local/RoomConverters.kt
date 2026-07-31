package com.example.tmdbmovies.data.local

import androidx.room.TypeConverter

internal class RoomConverters {
    @TypeConverter
    fun genreIdsToStorage(ids: List<Long>): String = ids.joinToString(",")

    @TypeConverter
    fun storageToGenreIds(value: String): List<Long> = value
        .takeIf(String::isNotBlank)
        ?.split(',')
        ?.mapNotNull(String::toLongOrNull)
        .orEmpty()
}
