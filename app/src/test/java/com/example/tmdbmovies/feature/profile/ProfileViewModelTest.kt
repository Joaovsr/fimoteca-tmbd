package com.example.tmdbmovies.feature.profile

import com.example.tmdbmovies.domain.model.Movie
import com.example.tmdbmovies.test.FakeFavoriteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before fun setUp() = Dispatchers.setMain(dispatcher)
    @After fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `collection count follows local favorites`() = runTest(dispatcher) {
        val repository = FakeFavoriteRepository(listOf(Movie(1, "One", null, null, null, null, null, emptyList())))
        val viewModel = ProfileViewModel(repository)
        backgroundScope.launch { viewModel.favoriteCount.collect {} }
        advanceUntilIdle()
        assertEquals(1, viewModel.favoriteCount.value)

        repository.favorites.value = emptyList()
        advanceUntilIdle()
        assertEquals(0, viewModel.favoriteCount.value)
    }
}
