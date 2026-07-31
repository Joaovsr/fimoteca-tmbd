package com.example.tmdbmovies.feature.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import coil3.compose.AsyncImage
import com.example.tmdbmovies.R
import com.example.tmdbmovies.core.ui.tmdbPosterUrl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    state: FavoritesUiState,
    onMovieClick: (Long) -> Unit,
    onRemoveClick: (Long) -> Unit,
    onBackClick: () -> Unit,
) {
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.favorites_title)) },
                navigationIcon = {
                    TextButton(onClick = onBackClick) { Text(stringResource(R.string.back)) }
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            when {
                state.isLoading -> CircularProgressIndicator(
                    Modifier.semantics { testTag = "favorites-loading" },
                )
                state.hasError -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.favorites_error))
                }
                state.movies.isEmpty() -> Text(
                    stringResource(R.string.favorites_empty),
                    modifier = Modifier.semantics { testTag = "favorites-empty" },
                )
                else -> FavoritesList(state.movies, onMovieClick, onRemoveClick)
            }
        }
    }
}

@Composable
private fun FavoritesList(
    movies: List<FavoriteMovieUiModel>,
    onMovieClick: (Long) -> Unit,
    onRemoveClick: (Long) -> Unit,
) = LazyColumn(
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
    modifier = Modifier.fillMaxSize(),
) {
    items(movies, key = FavoriteMovieUiModel::id) { movie ->
        FavoriteCard(movie, { onMovieClick(movie.id) }, { onRemoveClick(movie.id) })
    }
}

@Composable
private fun FavoriteCard(movie: FavoriteMovieUiModel, onClick: () -> Unit, onRemoveClick: () -> Unit) {
    val title = movie.title.ifBlank { stringResource(R.string.movie_title_unavailable) }
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().semantics { testTag = "favorite-card-${movie.id}" },
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = tmdbPosterUrl(movie.posterPath),
                contentDescription = stringResource(R.string.movie_poster_content_description, title),
                placeholder = painterResource(R.drawable.ic_movie_fallback),
                error = painterResource(R.drawable.ic_movie_fallback),
                fallback = painterResource(R.drawable.ic_movie_fallback),
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(width = 92.dp, height = 138.dp),
            )
            Spacer(Modifier.size(16.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(movie.releaseDate ?: stringResource(R.string.movie_date_unavailable))
                Button(
                    onClick = onRemoveClick,
                    modifier = Modifier.semantics { testTag = "favorite-remove-${movie.id}" },
                ) { Text(stringResource(R.string.favorite_remove)) }
            }
        }
    }
}
