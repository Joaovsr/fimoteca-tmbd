package com.example.tmdbmovies.data.remote

import com.example.tmdbmovies.core.network.TmdbAuthInterceptor
import com.example.tmdbmovies.data.remote.dto.MovieDto
import com.example.tmdbmovies.data.remote.dto.GenreListDto
import com.example.tmdbmovies.data.remote.dto.PagedResponseDto
import com.example.tmdbmovies.data.remote.mapper.toRemoteMoviePage
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class DiscoverMoviesParsingTest {
    private lateinit var server: MockWebServer
    private val fixture by lazy {
        requireNotNull(javaClass.classLoader?.getResource("fixtures/discover_movies.json")).readText()
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
    fun `discover parses maps nulls and sends expected request`() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(fixture),
        )
        val fakeToken = listOf("unit", "test").joinToString("-")
        val api = createApi(fakeToken)

        val page =
            api.discoverMovies(
                page = 2,
                sortBy = "popularity.desc",
                genreId = 18,
                minimumRating = 6.5,
                releaseYear = 2026,
            ).toRemoteMoviePage()

        val request = server.takeRequest()
        assertEquals("GET", request.method)
        assertEquals("/3/discover/movie", request.requestUrl?.encodedPath)
        assertEquals("2", request.requestUrl?.queryParameter("page"))
        assertEquals("pt-BR", request.requestUrl?.queryParameter("language"))
        assertEquals("BR", request.requestUrl?.queryParameter("region"))
        assertEquals("false", request.requestUrl?.queryParameter("include_adult"))
        assertEquals("popularity.desc", request.requestUrl?.queryParameter("sort_by"))
        assertEquals("18", request.requestUrl?.queryParameter("with_genres"))
        assertEquals("6.5", request.requestUrl?.queryParameter("vote_average.gte"))
        assertEquals("2026", request.requestUrl?.queryParameter("primary_release_year"))
        assertEquals("application/json", request.getHeader("Accept"))
        assertEquals("Bearer $fakeToken", request.getHeader("Authorization"))

        assertEquals(2, page.page)
        assertEquals(8, page.totalPages)
        assertEquals(143, page.totalResults)
        assertEquals(2, page.movies.size)
        assertEquals(101L, page.movies.first().movieId)
        assertEquals("Filme válido", page.movies.first().title)
        assertNull(page.movies.first().overview)
        assertEquals("/poster.jpg", page.movies.first().posterPath)
        assertNull(page.movies.first().releaseDate)
        assertEquals(emptyList<Long>(), page.movies[1].genreIds)
        assertEquals("", page.movies[1].title)
        assertNull(page.movies[1].releaseDate)
    }

    @Test
    fun `blank token does not add authorization header`() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"page":1,"results":[],"total_pages":0,"total_results":0}"""),
        )

        createApi("  ").discoverMovies(page = 1)

        assertNull(server.takeRequest().getHeader("Authorization"))
    }

    @Test
    fun `fixture can be decoded independently of Retrofit`() {
        val response =
            json.decodeFromString<PagedResponseDto<MovieDto>>(fixture)

        assertEquals(3, response.results.size)
        assertNull(response.results[1].title)
        assertNull(response.results[2].id)
    }

    @Test
    fun `genre endpoint parses valid values and uses app language`() = runBlocking {
        val genresFixture = requireNotNull(javaClass.classLoader?.getResource("fixtures/genres.json")).readText()
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(genresFixture),
        )

        val response = createApi(" ").genres()
        val request = server.takeRequest()

        assertEquals("/3/genre/movie/list", request.requestUrl?.encodedPath)
        assertEquals("pt-BR", request.requestUrl?.queryParameter("language"))
        assertEquals(3, response.genres.size)
        assertNull(request.getHeader("Authorization"))
        assertEquals(28L, json.decodeFromString<GenreListDto>(genresFixture).genres.first().id)
    }

    @Test
    fun `search sends only query pagination and compatible year parameters`() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("""{"page":1,"results":[],"total_pages":0,"total_results":0}"""),
        )

        createApi(" ").searchMovies(query = "Alien", page = 3, releaseYear = 1979)
        val request = server.takeRequest()

        assertEquals("/3/search/movie", request.requestUrl?.encodedPath)
        assertEquals("Alien", request.requestUrl?.queryParameter("query"))
        assertEquals("3", request.requestUrl?.queryParameter("page"))
        assertEquals("1979", request.requestUrl?.queryParameter("primary_release_year"))
        assertEquals("false", request.requestUrl?.queryParameter("include_adult"))
        assertNull(request.requestUrl?.queryParameter("sort_by"))
        assertNull(request.requestUrl?.queryParameter("with_genres"))
        assertNull(request.requestUrl?.queryParameter("vote_average.gte"))
    }

    private fun createApi(accessToken: String): TmdbApi {
        val client =
            OkHttpClient.Builder()
                .addInterceptor(TmdbAuthInterceptor(accessToken))
                .build()

        return Retrofit.Builder()
            .baseUrl(server.url("/3/"))
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(TmdbApi::class.java)
    }

    private companion object {
        val json = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
    }
}
