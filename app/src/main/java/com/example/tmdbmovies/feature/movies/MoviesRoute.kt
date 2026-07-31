package com.example.tmdbmovies.feature.movies

import androidx.compose.runtime.Composable
import androidx.paging.compose.collectAsLazyPagingItems

@Composable
fun MoviesRoute(viewModel: MoviesViewModel, onMovieClick: (Long) -> Unit) {
    MoviesScreen(
        movies = viewModel.movies.collectAsLazyPagingItems(),
        onMovieClick = onMovieClick,
    )
}
