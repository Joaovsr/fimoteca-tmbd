package com.example.tmdbmovies.feature.discover

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun DiscoverRoute(
    viewModel: DiscoverViewModel,
    onMovieClick: (Long) -> Unit,
    onSearchClick: () -> Unit,
) {
    DiscoverScreen(
        state = viewModel.state.collectAsStateWithLifecycle().value,
        onMovieClick = onMovieClick,
        onFavoriteClick = viewModel::onFavoriteClick,
        onRetry = viewModel::retry,
        onFeaturedSelected = viewModel::selectFeatured,
        onSearchClick = onSearchClick,
    )
}
