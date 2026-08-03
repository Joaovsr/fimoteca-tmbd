package com.example.tmdbmovies.feature.movies

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems

@Composable
fun MoviesRoute(
    viewModel: MoviesViewModel,
    onMovieClick: (Long) -> Unit,
    searchMode: Boolean = false,
    onSearchClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
) {
    MoviesScreen(
        movies = viewModel.movies.collectAsLazyPagingItems(),
        uiState = viewModel.state.collectAsStateWithLifecycle().value,
        onQueryChanged = viewModel::onQueryChanged,
        onFiltersChanged = viewModel::onFiltersChanged,
        onClearFilters = viewModel::clearFilters,
        onRetryGenres = viewModel::retryGenres,
        onMovieClick = onMovieClick,
        onFavoriteClick = viewModel::onFavoriteClick,
        searchMode = searchMode,
        onSearchClick = onSearchClick,
        onBackClick = onBackClick,
    )
}
