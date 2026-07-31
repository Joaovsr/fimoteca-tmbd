package com.example.tmdbmovies.feature.movies

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.tmdbmovies.R
import com.example.tmdbmovies.core.common.AppError
import com.example.tmdbmovies.core.common.AppResult
import com.example.tmdbmovies.domain.model.Genre
import com.example.tmdbmovies.domain.model.MovieFilters
import com.example.tmdbmovies.domain.model.MovieSortOrder
import com.example.tmdbmovies.domain.model.compatibleWithQuery
import com.example.tmdbmovies.domain.repository.MovieRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MoviesViewModel(
    private val repository: MovieRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val query = MutableStateFlow(savedStateHandle[QUERY_KEY] ?: "")
    private val filters = MutableStateFlow(restoredFilters())
    private val genresState = MutableStateFlow(GenresState(isLoading = true))

    val state: StateFlow<MoviesUiState> = combine(query, filters, genresState) { query, filters, genres ->
        MoviesUiState(
            query = query,
            filters = filters,
            genres = genres.items,
            isLoadingGenres = genres.isLoading,
            genresErrorMessageRes = genres.errorMessageRes,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = MoviesUiState(query.value, filters.value, isLoadingGenres = true),
    )

    private val requests = query
        .transformLatest { rawQuery ->
            val normalizedQuery = rawQuery.trim()
            if (normalizedQuery.isNotEmpty()) delay(SEARCH_DEBOUNCE_MILLIS)
            emit(normalizedQuery)
        }
        .distinctUntilChanged()
        .combine(filters) { query, filters ->
            MoviesRequest(query, filters.compatibleWithQuery(query))
        }
        .distinctUntilChanged()

    val movies: Flow<PagingData<MovieUiModel>> = requests
        .flatMapLatest { request -> repository.pagedMovies(request.query, request.filters) }
        .map { pagingData -> pagingData.map { it.toUiModel() } }
        .cachedIn(viewModelScope)

    init {
        loadGenres()
    }

    fun onQueryChanged(value: String) {
        query.value = value
        savedStateHandle[QUERY_KEY] = value
    }

    fun onGenreChanged(genreId: Long?) = updateFilters(filters.value.copy(genreId = genreId))

    fun onSortOrderChanged(sortOrder: MovieSortOrder) =
        updateFilters(filters.value.copy(sortOrder = sortOrder))

    fun onMinimumRatingChanged(minimumRating: Double?) =
        updateFilters(filters.value.copy(minimumRating = minimumRating))

    fun onReleaseYearChanged(releaseYear: Int?) =
        updateFilters(filters.value.copy(releaseYear = releaseYear))

    fun onFiltersChanged(value: MovieFilters) = updateFilters(value)

    fun clearFilters() = updateFilters(MovieFilters())

    fun retryGenres() = loadGenres()

    private fun updateFilters(value: MovieFilters) {
        filters.value = value
        savedStateHandle[GENRE_KEY] = value.genreId
        savedStateHandle[SORT_KEY] = value.sortOrder.name
        savedStateHandle[RATING_KEY] = value.minimumRating
        savedStateHandle[YEAR_KEY] = value.releaseYear
    }

    private fun loadGenres() {
        genresState.value = genresState.value.copy(isLoading = true, errorMessageRes = null)
        viewModelScope.launch {
            genresState.value = when (val result = repository.genres()) {
                is AppResult.Success -> GenresState(items = result.value.sortedBy { it.name })
                is AppResult.Failure -> GenresState(errorMessageRes = result.error.messageRes())
            }
        }
    }

    private fun restoredFilters(): MovieFilters = MovieFilters(
        genreId = savedStateHandle[GENRE_KEY],
        sortOrder = savedStateHandle.get<String>(SORT_KEY)
            ?.let { name -> MovieSortOrder.entries.firstOrNull { it.name == name } }
            ?: MovieSortOrder.PopularityDescending,
        minimumRating = savedStateHandle[RATING_KEY],
        releaseYear = savedStateHandle[YEAR_KEY],
    )

    private data class MoviesRequest(val query: String, val filters: MovieFilters)

    private data class GenresState(
        val items: List<Genre> = emptyList(),
        val isLoading: Boolean = false,
        val errorMessageRes: Int? = null,
    )

    companion object {
        const val SEARCH_DEBOUNCE_MILLIS = 400L

        private const val QUERY_KEY = "movies.query"
        private const val GENRE_KEY = "movies.genre"
        private const val SORT_KEY = "movies.sort"
        private const val RATING_KEY = "movies.rating"
        private const val YEAR_KEY = "movies.year"
    }
}

private fun AppError.messageRes(): Int = when (this) {
    AppError.NoConnection -> R.string.error_no_connection
    AppError.Timeout -> R.string.error_timeout
    AppError.Unauthorized -> R.string.error_unauthorized
    AppError.RateLimited -> R.string.error_rate_limited
    AppError.NotFound,
    is AppError.UnexpectedHttp,
    AppError.InvalidData,
    AppError.Unknown,
    -> R.string.error_genres_generic
}
