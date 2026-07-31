package com.example.tmdbmovies.data.remote

import com.example.tmdbmovies.data.remote.dto.MovieDto
import com.example.tmdbmovies.data.remote.dto.MovieDetailsDto
import com.example.tmdbmovies.data.remote.dto.PagedResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

internal interface TmdbApi {
    @GET("discover/movie")
    suspend fun discoverMovies(
        @Query("page") page: Int,
        @Query("language") language: String = "pt-BR",
        @Query("region") region: String = "BR",
        @Query("include_adult") includeAdult: Boolean = false,
        @Query("sort_by") sortBy: String? = null,
        @Query("with_genres") genreId: Long? = null,
        @Query("vote_average.gte") minimumRating: Double? = null,
        @Query("primary_release_year") releaseYear: Int? = null,
    ): PagedResponseDto<MovieDto>

    @GET("movie/{movie_id}")
    suspend fun movieDetails(
        @Path("movie_id") movieId: Long,
        @Query("language") language: String = "pt-BR",
    ): MovieDetailsDto
}
