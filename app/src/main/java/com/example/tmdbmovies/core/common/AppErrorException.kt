package com.example.tmdbmovies.core.common

/** A Paging-compatible failure that preserves the domain error category. */
class AppErrorException(
    val appError: AppError,
    cause: Throwable,
) : RuntimeException(cause)
