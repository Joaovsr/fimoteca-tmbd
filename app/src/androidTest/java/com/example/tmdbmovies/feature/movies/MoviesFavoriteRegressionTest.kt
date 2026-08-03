package com.example.tmdbmovies.feature.movies

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import androidx.paging.PagingData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.tmdbmovies.core.common.AppResult
import com.example.tmdbmovies.core.ui.theme.TmdbMoviesTheme
import com.example.tmdbmovies.domain.model.Genre
import com.example.tmdbmovies.domain.model.Movie
import com.example.tmdbmovies.domain.model.MovieDetails
import com.example.tmdbmovies.domain.model.MovieFilters
import com.example.tmdbmovies.domain.repository.FavoriteRepository
import com.example.tmdbmovies.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.junit.Rule
import org.junit.Test

class MoviesFavoriteRegressionTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun removingFavoriteRemapsVisiblePageWithoutCrashing() {
        val movie = Movie(42, "Movie", null, null, null, null, null, emptyList())
        val favorites = FakeFavoriteRepository(movie)
        val viewModel = MoviesViewModel(FakeMovieRepository(movie), favorites, SavedStateHandle())
        composeRule.setContent {
            TmdbMoviesTheme {
                MoviesRoute(viewModel, onMovieClick = {})
            }
        }

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Remover dos favoritos").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("movie-favorite-42").performClick()

        composeRule.onNodeWithText("Salvar como favorito").assertExists()
    }

    private class FakeMovieRepository(private val movie: Movie) : MovieRepository {
        override fun pagedMovies(query: String, filters: MovieFilters): Flow<PagingData<Movie>> =
            Pager(PagingConfig(pageSize = 20)) { SingleMoviePagingSource(movie) }.flow

        override suspend fun movies(collection: com.example.tmdbmovies.domain.model.MovieCollection): AppResult<List<Movie>> = AppResult.Success(emptyList())

        override suspend fun genres(): AppResult<List<Genre>> = AppResult.Success(emptyList())

        override suspend fun movieDetails(movieId: Long): AppResult<MovieDetails> =
            error("Details are not used")
    }

    private class SingleMoviePagingSource(private val movie: Movie) : PagingSource<Int, Movie>() {
        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Movie> =
            LoadResult.Page(data = listOf(movie), prevKey = null, nextKey = null)

        override fun getRefreshKey(state: PagingState<Int, Movie>): Int? = null
    }

    private class FakeFavoriteRepository(movie: Movie) : FavoriteRepository {
        private val favorites = MutableStateFlow(listOf(movie))

        override fun observeFavorites(): Flow<List<Movie>> = favorites

        override fun observeFavorite(movieId: Long): Flow<Boolean> =
            favorites.map { movies -> movies.any { it.movieId == movieId } }

        override suspend fun setFavorite(movie: Movie, favorite: Boolean): AppResult<Unit> {
            favorites.value = if (favorite) listOf(movie) else emptyList()
            return AppResult.Success(Unit)
        }
    }
}
