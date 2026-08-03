package com.example.tmdbmovies.feature.favorites

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tmdbmovies.domain.model.Movie
import com.example.tmdbmovies.domain.repository.FavoriteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val repository: FavoriteRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val query = MutableStateFlow(savedStateHandle[QUERY_KEY] ?: "")
    private val sortOrder = MutableStateFlow(restoredSortOrder())

    val state = combine(repository.observeFavorites(), query, sortOrder) { movies, query, sortOrder ->
        val uiMovies = movies.map(Movie::toUiModel)
        FavoritesUiState(
            movies = uiMovies
                .filter { query.isBlank() || it.title.contains(query.trim(), ignoreCase = true) }
                .sorted(sortOrder),
            totalCount = uiMovies.size,
            query = query,
            sortOrder = sortOrder,
            isLoading = false,
        )
    }
        .onStart { emit(FavoritesUiState(isLoading = true)) }
        .catch { emit(FavoritesUiState(isLoading = false, hasError = true)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FavoritesUiState(),
        )

    fun removeFavorite(movieId: Long) {
        val movie = state.value.movies.firstOrNull { it.id == movieId } ?: return
        viewModelScope.launch {
            repository.setFavorite(movie.toDomain(), false)
        }
    }

    fun onQueryChanged(value: String) {
        query.value = value
        savedStateHandle[QUERY_KEY] = value
    }

    fun onSortOrderChanged(value: FavoriteSortOrder) {
        sortOrder.value = value
        savedStateHandle[SORT_KEY] = value.name
    }

    private fun restoredSortOrder(): FavoriteSortOrder = savedStateHandle.get<String>(SORT_KEY)
        ?.let { saved -> FavoriteSortOrder.entries.firstOrNull { it.name == saved } }
        ?: FavoriteSortOrder.RecentlyAdded

    private companion object {
        const val QUERY_KEY = "favorites.query"
        const val SORT_KEY = "favorites.sort"
    }
}

private fun Movie.toUiModel() = FavoriteMovieUiModel(
    id = movieId,
    title = title.trim(),
    releaseDate = releaseDate?.trim()?.takeIf(String::isNotEmpty),
    posterPath = posterPath,
    voteAverage = voteAverage,
)

private fun List<FavoriteMovieUiModel>.sorted(order: FavoriteSortOrder): List<FavoriteMovieUiModel> = when (order) {
    FavoriteSortOrder.RecentlyAdded -> this
    FavoriteSortOrder.TitleAscending -> sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title })
    FavoriteSortOrder.RatingDescending -> sortedWith(compareByDescending<FavoriteMovieUiModel> { it.voteAverage ?: Double.NEGATIVE_INFINITY }.thenBy { it.title })
    FavoriteSortOrder.ReleaseDateDescending -> sortedWith(compareByDescending<FavoriteMovieUiModel> { it.releaseDate.orEmpty() }.thenBy { it.title })
    FavoriteSortOrder.ReleaseDateAscending -> sortedWith(compareBy<FavoriteMovieUiModel> { it.releaseDate ?: "9999" }.thenBy { it.title })
}

private fun FavoriteMovieUiModel.toDomain() = Movie(
    movieId = id,
    title = title,
    overview = null,
    posterPath = posterPath,
    backdropPath = null,
    releaseDate = releaseDate,
    voteAverage = voteAverage,
    genreIds = emptyList(),
)
