package com.example.tmdbmovies.feature.movies

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import coil3.compose.AsyncImage
import com.example.tmdbmovies.R
import com.example.tmdbmovies.core.common.AppError
import com.example.tmdbmovies.core.common.AppErrorException
import com.example.tmdbmovies.core.ui.tmdbPosterUrl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoviesScreen(
    movies: LazyPagingItems<MovieUiModel>,
    onMovieClick: (Long) -> Unit,
) {
    val listState = rememberLazyListState()
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.movies_title)) }) },
    ) { padding ->
        when (val refresh = movies.loadState.refresh) {
            is LoadState.Loading if movies.itemCount == 0 -> LoadingContent(Modifier.padding(padding))
            is LoadState.Error if movies.itemCount == 0 -> MoviesErrorContent(
                message = errorMessage(refresh.error),
                onRetry = movies::retry,
                modifier = Modifier.padding(padding),
            )
            is LoadState.NotLoading if movies.itemCount == 0 -> EmptyContent(Modifier.padding(padding))
            else -> MoviesListContent(movies, listState, padding, refresh, onMovieClick)
        }
    }
}

@Composable
private fun MoviesListContent(
    movies: LazyPagingItems<MovieUiModel>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    padding: PaddingValues,
    refresh: LoadState,
    onMovieClick: (Long) -> Unit,
) = LazyColumn(
    state = listState,
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
    modifier = Modifier.fillMaxSize().padding(padding),
) {
    if (refresh is LoadState.Loading) item { RefreshLoading() }
    if (refresh is LoadState.Error) item {
        AppendError(message = errorMessage(refresh.error), onRetry = movies::retry)
    }
    items(
        count = movies.itemCount,
        key = movies.itemKey(MovieUiModel::id),
    ) { index ->
        movies[index]?.let { movie -> MovieCard(movie = movie, onClick = { onMovieClick(movie.id) }) }
    }
    when (val append = movies.loadState.append) {
        is LoadState.Loading -> item { AppendLoading() }
        is LoadState.Error -> item { AppendError(message = errorMessage(append.error), onRetry = movies::retry) }
        is LoadState.NotLoading -> Unit
    }
}

@Composable
private fun MovieCard(movie: MovieUiModel, onClick: () -> Unit) {
    val title = movie.title.ifBlank { stringResource(R.string.movie_title_unavailable) }
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().semantics { testTag = "movie-card-${movie.id}" },
        colors = CardDefaults.cardColors(),
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = tmdbPosterUrl(movie.posterPath),
                contentDescription = stringResource(R.string.movie_poster_content_description, title),
                placeholder = painterResource(R.drawable.ic_movie_fallback),
                error = painterResource(R.drawable.ic_movie_fallback),
                fallback = painterResource(R.drawable.ic_movie_fallback),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(width = 92.dp, height = 138.dp)
                    .then(
                        if (movie.posterPath.isNullOrBlank()) {
                            Modifier.semantics { testTag = "movie-poster-fallback" }
                        } else {
                            Modifier
                        },
                    ),
            )
            Spacer(Modifier.size(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    movie.releaseDate ?: stringResource(R.string.movie_date_unavailable),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) = Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.Center,
) { CircularProgressIndicator() }

@Composable
internal fun EmptyContent(modifier: Modifier = Modifier) = Box(
    modifier = modifier.fillMaxSize().semantics { testTag = "movies-empty" },
    contentAlignment = Alignment.Center,
) { Text(stringResource(R.string.movies_empty)) }

@Composable
internal fun MoviesErrorContent(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) = Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.Center,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
        Text(message)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry, modifier = Modifier.semantics { testTag = "movies-retry" }) {
            Text(stringResource(R.string.retry))
        }
    }
}

@Composable
private fun AppendLoading() = Box(Modifier.fillMaxWidth().padding(16.dp), Alignment.Center) {
    CircularProgressIndicator(modifier = Modifier.size(28.dp))
}

@Composable
private fun RefreshLoading() = Box(Modifier.fillMaxWidth().padding(8.dp), Alignment.Center) {
    CircularProgressIndicator(modifier = Modifier.size(24.dp))
}

@Composable
private fun AppendError(message: String, onRetry: () -> Unit) = Column(
    modifier = Modifier.fillMaxWidth().padding(8.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
) {
    Text(message)
    Button(onClick = onRetry) { Text(stringResource(R.string.retry)) }
}

@Composable
private fun errorMessage(error: Throwable): String = when ((error as? AppErrorException)?.appError) {
    AppError.NoConnection -> stringResource(R.string.error_no_connection)
    AppError.Timeout -> stringResource(R.string.error_timeout)
    AppError.Unauthorized -> stringResource(R.string.error_unauthorized)
    AppError.RateLimited -> stringResource(R.string.error_rate_limited)
    AppError.NotFound,
    is AppError.UnexpectedHttp,
    AppError.InvalidData,
    AppError.Unknown,
    null,
    -> stringResource(R.string.error_generic)
}
