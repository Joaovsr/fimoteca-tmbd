package com.example.tmdbmovies.core.network

import okhttp3.Interceptor
import okhttp3.Response

internal class TmdbAuthInterceptor(
    accessToken: String,
) : Interceptor {
    private val accessToken = accessToken.trim()

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder =
            chain.request()
                .newBuilder()
                .header("Accept", "application/json")

        if (accessToken.isNotEmpty()) {
            requestBuilder.header("Authorization", "Bearer $accessToken")
        }

        return chain.proceed(requestBuilder.build())
    }
}
