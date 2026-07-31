package com.example.tmdbmovies.core.network

import com.example.tmdbmovies.core.common.AppError
import java.io.IOException
import java.net.SocketTimeoutException
import kotlinx.serialization.SerializationException
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class NetworkErrorMapperTest {
    @Test
    fun `network failures map to stable domain errors`() {
        assertEquals(AppError.NoConnection, IOException().toAppError())
        assertEquals(AppError.Timeout, SocketTimeoutException().toAppError())
    }

    @Test
    fun `http failures map status categories`() {
        assertEquals(AppError.Unauthorized, httpException(401).toAppError())
        assertEquals(AppError.RateLimited, httpException(429).toAppError())
        assertEquals(AppError.UnexpectedHttp(503), httpException(503).toAppError())
    }

    @Test
    fun `serialization and unexpected failures map without leaking technical types`() {
        assertEquals(AppError.InvalidData, SerializationException("invalid").toAppError())
        assertEquals(AppError.Unknown, IllegalStateException().toAppError())
    }

    private fun httpException(statusCode: Int): HttpException =
        HttpException(Response.error<Unit>(statusCode, "".toResponseBody()))
}
