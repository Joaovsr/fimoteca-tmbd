package com.example.tmdbmovies.data.remote

import com.example.tmdbmovies.data.remote.dto.MovieDto
import com.example.tmdbmovies.data.remote.dto.PagedResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

internal interface TmdbEditorialApi {
    @GET("trending/movie/week")
    suspend fun trendingMovies(@Query("language") language: String = "pt-BR"): PagedResponseDto<MovieDto>

    @GET("movie/now_playing")
    suspend fun nowPlayingMovies(
        @Query("page") page: Int = 1,
        @Query("language") language: String = "pt-BR",
        @Query("region") region: String = "BR",
    ): PagedResponseDto<MovieDto>

    @GET("movie/top_rated")
    suspend fun topRatedMovies(
        @Query("page") page: Int = 1,
        @Query("language") language: String = "pt-BR",
        @Query("region") region: String = "BR",
    ): PagedResponseDto<MovieDto>

    @GET("discover/movie")
    suspend fun classicMovies(
        @Query("page") page: Int = 1,
        @Query("language") language: String = "pt-BR",
        @Query("region") region: String = "BR",
        @Query("include_adult") includeAdult: Boolean = false,
        @Query("sort_by") sortBy: String = "vote_average.desc",
        @Query("vote_average.gte") minimumRating: Double = 7.0,
        @Query("primary_release_date.lte") releaseDateUntil: String = "2005-12-31",
        @Query("vote_count.gte") minimumVoteCount: Int = 1000,
    ): PagedResponseDto<MovieDto>
}
