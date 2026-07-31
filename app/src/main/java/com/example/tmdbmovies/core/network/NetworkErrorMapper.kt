package com.example.tmdbmovies.core.network

import com.example.tmdbmovies.core.common.AppError
import java.io.IOException
import java.net.SocketTimeoutException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

internal fun Throwable.toAppError(): AppError =
    when (this) {
        is SocketTimeoutException -> AppError.Timeout
        is HttpException ->
            when (code()) {
                401 -> AppError.Unauthorized
                429 -> AppError.RateLimited
                else -> AppError.UnexpectedHttp(code())
            }
        is SerializationException -> AppError.InvalidData
        is IOException -> AppError.NoConnection
        else -> AppError.Unknown
    }
