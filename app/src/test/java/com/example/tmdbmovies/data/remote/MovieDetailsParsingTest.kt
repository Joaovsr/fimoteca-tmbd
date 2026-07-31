package com.example.tmdbmovies.data.remote

import com.example.tmdbmovies.data.remote.dto.MovieDetailsDto
import com.example.tmdbmovies.data.remote.mapper.toDomain
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class MovieDetailsParsingTest {
    private lateinit var server: MockWebServer
    private val fixture by lazy {
        requireNotNull(javaClass.classLoader?.getResource("fixtures/movie_details.json")).readText()
    }

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `details endpoint sends id and language then maps response`() = runBlocking {
        server.enqueue(MockResponse().setHeader("Content-Type", "application/json").setBody(fixture))
        val api = Retrofit.Builder()
            .baseUrl(server.url("/3/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(TmdbApi::class.java)

        val details = requireNotNull(api.movieDetails(550).toDomain())

        val request = server.takeRequest()
        assertEquals("/3/movie/550", request.requestUrl?.encodedPath)
        assertEquals("pt-BR", request.requestUrl?.queryParameter("language"))
        assertEquals(550L, details.movieId)
        assertEquals("Clube da Luta", details.title)
        assertEquals("Um homem encontra uma forma radical de mudar sua vida.", details.overview)
        assertEquals("/poster.jpg", details.posterPath)
        assertNull(details.backdropPath)
        assertEquals("1999-10-15", details.releaseDate)
    }

    @Test
    fun `missing fields map to safe nullable domain values`() {
        val details = requireNotNull(
            json.decodeFromString<MovieDetailsDto>("""{"id":42,"title":null,"release_date":""}""")
                .toDomain(),
        )

        assertEquals("", details.title)
        assertNull(details.overview)
        assertNull(details.posterPath)
        assertNull(details.releaseDate)
    }

    private companion object {
        val json = Json { ignoreUnknownKeys = true; explicitNulls = false }
    }
}
