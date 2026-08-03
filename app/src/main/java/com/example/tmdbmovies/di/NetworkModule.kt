package com.example.tmdbmovies.di

import com.example.tmdbmovies.BuildConfig
import com.example.tmdbmovies.core.network.TmdbAuthInterceptor
import com.example.tmdbmovies.data.remote.TmdbApi
import com.example.tmdbmovies.data.remote.TmdbEditorialApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

private const val TMDB_BASE_URL = "https://api.themoviedb.org/3/"

internal val networkModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
    }
    single {
        OkHttpClient.Builder()
            .addInterceptor(TmdbAuthInterceptor(BuildConfig.TMDB_ACCESS_TOKEN))
            .build()
    }
    single {
        Retrofit.Builder()
            .baseUrl(TMDB_BASE_URL)
            .client(get())
            .addConverterFactory(get<Json>().asConverterFactory("application/json".toMediaType()))
            .build()
    }
    single<TmdbApi> { get<Retrofit>().create(TmdbApi::class.java) }
    single<TmdbEditorialApi> { get<Retrofit>().create(TmdbEditorialApi::class.java) }
}
