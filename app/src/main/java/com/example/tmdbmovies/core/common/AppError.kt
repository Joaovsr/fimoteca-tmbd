package com.example.tmdbmovies.core.common

sealed interface AppError {
    data object NoConnection : AppError

    data object Timeout : AppError

    data object Unauthorized : AppError

    data object RateLimited : AppError

    data class UnexpectedHttp(val statusCode: Int) : AppError

    data object InvalidData : AppError

    data object Unknown : AppError
}
