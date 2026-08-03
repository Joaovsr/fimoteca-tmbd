package com.example.tmdbmovies.feature.details

import android.os.Bundle
import androidx.navigation.Navigation
import androidx.paging.PagingData
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.example.tmdbmovies.R
import com.example.tmdbmovies.app.MainActivity
import com.example.tmdbmovies.core.common.AppError
import com.example.tmdbmovies.core.common.AppResult
import com.example.tmdbmovies.domain.model.Movie
import com.example.tmdbmovies.domain.model.MovieDetails
import com.example.tmdbmovies.domain.model.MovieFilters
import com.example.tmdbmovies.domain.model.Genre
import com.example.tmdbmovies.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Assert.assertEquals
import org.junit.Test
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.dsl.module

class MovieDetailsFragmentTest {
    @Test
    fun errorRetryRendersDetailsAndUsesNavigationMovieId() {
        val repository = FakeRepository()
        val testModule = module {
            single<MovieRepository> { repository }
        }
        loadKoinModules(testModule)

        try {
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                scenario.onActivity { activity ->
                    Navigation.findNavController(activity, R.id.nav_host_fragment).navigate(
                        R.id.movieDetailsFragment,
                        Bundle().apply { putLong("movieId", 550L) },
                    )
                }

                onView(withText(R.string.error_no_connection)).check(matches(isDisplayed()))
                onView(withText(R.string.retry)).perform(click())
                onView(withText("Clube da Luta")).check(matches(isDisplayed()))
                onView(withText(R.string.movie_overview_unavailable)).check(matches(isDisplayed()))
                onView(withText("Lançamento · 15/10/1999")).check(matches(isDisplayed()))
                onView(withText("★ 8.4")).check(matches(isDisplayed()))

                scenario.recreate()
                onView(withText("Clube da Luta")).check(matches(isDisplayed()))
            }

            assertEquals(listOf(550L, 550L), repository.requests)
        } finally {
            unloadKoinModules(testModule)
        }
    }

    private class FakeRepository : MovieRepository {
        val requests = mutableListOf<Long>()

        override fun pagedMovies(query: String, filters: MovieFilters): Flow<PagingData<Movie>> = emptyFlow()

        override suspend fun movies(collection: com.example.tmdbmovies.domain.model.MovieCollection): AppResult<List<Movie>> = AppResult.Success(emptyList())

        override suspend fun genres(): AppResult<List<Genre>> = AppResult.Success(emptyList())

        override suspend fun movieDetails(movieId: Long): AppResult<MovieDetails> {
            requests += movieId
            return if (requests.size == 1) {
                AppResult.Failure(AppError.NoConnection)
            } else {
                AppResult.Success(
                    MovieDetails(
                        movieId = movieId,
                        title = "Clube da Luta",
                        overview = null,
                        posterPath = null,
                        backdropPath = null,
                        releaseDate = "1999-10-15",
                        voteAverage = 8.4,
                    ),
                )
            }
        }
    }
}
