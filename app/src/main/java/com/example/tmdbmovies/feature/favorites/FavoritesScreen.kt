package com.example.tmdbmovies.feature.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.tmdbmovies.R
import com.example.tmdbmovies.core.ui.theme.MovieAccent
import com.example.tmdbmovies.core.ui.tmdbPosterUrl
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    state: FavoritesUiState,
    onMovieClick: (Long) -> Unit,
    onRemoveClick: (Long) -> Unit,
    onQueryChanged: (String) -> Unit = {},
    onSortOrderChanged: (FavoriteSortOrder) -> Unit = {},
    onExploreClick: () -> Unit = {},
) {
    var showSortSheet by remember { mutableStateOf(false) }
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.favorites_title), style = MaterialTheme.typography.headlineMedium)
                        if (!state.isLoading && !state.hasError) {
                            Text(
                                pluralStringResource(R.plurals.favorites_count, state.totalCount, state.totalCount),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showSortSheet = true },
                        enabled = state.totalCount > 1,
                        modifier = Modifier.semantics { testTag = "favorites-sort" },
                    ) {
                        Icon(painterResource(R.drawable.ic_sort), stringResource(R.string.favorites_sort))
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (state.totalCount > 0 || state.query.isNotEmpty()) {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = onQueryChanged,
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.favorites_search_hint)) },
                    leadingIcon = { Icon(painterResource(R.drawable.ic_search), null) },
                    trailingIcon = {
                        if (state.query.isNotEmpty()) TextButton(onClick = { onQueryChanged("") }) {
                            Text(stringResource(R.string.clear))
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                        .semantics { testTag = "favorites-search" },
                )
            }
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                when {
                    state.isLoading -> CircularProgressIndicator(Modifier.semantics { testTag = "favorites-loading" })
                    state.hasError -> Text(stringResource(R.string.favorites_error))
                    state.totalCount == 0 -> EmptyFavoritesState(onExploreClick)
                    state.movies.isEmpty() -> NoFavoriteResults()
                    else -> FavoritesGrid(state.movies, onMovieClick, onRemoveClick)
                }
            }
        }
    }

    if (showSortSheet) {
        SortBottomSheet(
            selected = state.sortOrder,
            onSelected = {
                onSortOrderChanged(it)
                showSortSheet = false
            },
            onDismiss = { showSortSheet = false },
        )
    }
}

@Composable
private fun FavoritesGrid(
    movies: List<FavoriteMovieUiModel>,
    onMovieClick: (Long) -> Unit,
    onRemoveClick: (Long) -> Unit,
) = LazyVerticalGrid(
    columns = GridCells.Fixed(3),
    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 16.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalArrangement = Arrangement.spacedBy(20.dp),
    modifier = Modifier.fillMaxSize().semantics { testTag = "favorites-grid" },
) {
    items(movies, key = FavoriteMovieUiModel::id) { movie ->
        FavoritePosterCard(movie, { onMovieClick(movie.id) }, { onRemoveClick(movie.id) })
    }
}

@Composable
private fun FavoritePosterCard(movie: FavoriteMovieUiModel, onClick: () -> Unit, onRemoveClick: () -> Unit) {
    val title = movie.title.ifBlank { stringResource(R.string.movie_title_unavailable) }
    Column(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
            .semantics { testTag = "favorite-card-${movie.id}" },
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            Modifier.fillMaxWidth().aspectRatio(2f / 3f).clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            AsyncImage(
                model = tmdbPosterUrl(movie.posterPath),
                contentDescription = stringResource(R.string.movie_poster_content_description, title),
                placeholder = painterResource(R.drawable.ic_movie_fallback),
                error = painterResource(R.drawable.ic_movie_fallback),
                fallback = painterResource(R.drawable.ic_movie_fallback),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(Modifier.align(Alignment.TopEnd).padding(6.dp).size(48.dp), contentAlignment = Alignment.Center) {
                Surface(Modifier.size(40.dp), shape = CircleShape, color = Color.Black.copy(alpha = .68f)) {}
                IconButton(
                    onClick = onRemoveClick,
                    modifier = Modifier.fillMaxSize().semantics { testTag = "favorite-remove-${movie.id}" },
                ) {
                    Icon(
                        painterResource(R.drawable.ic_favorite_filled),
                        stringResource(R.string.favorite_remove_description, title),
                        tint = MovieAccent,
                    )
                }
            }
        }
        Text(title, style = MaterialTheme.typography.titleSmall, maxLines = 2, minLines = 2, overflow = TextOverflow.Ellipsis)
        Text(
            favoriteMetadata(movie),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}

@Composable
private fun EmptyFavoritesState(onExploreClick: () -> Unit) = Column(
    modifier = Modifier.padding(32.dp).semantics { testTag = "favorites-empty" },
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(12.dp),
) {
    Icon(painterResource(R.drawable.ic_favorite_outline), null, Modifier.size(56.dp), tint = MovieAccent)
    Text(stringResource(R.string.favorites_empty_title), style = MaterialTheme.typography.titleLarge)
    Text(
        stringResource(R.string.favorites_empty),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Button(onClick = onExploreClick, modifier = Modifier.semantics { testTag = "favorites-explore" }) {
        Text(stringResource(R.string.favorites_explore))
    }
}

@Composable
private fun NoFavoriteResults() = Column(
    modifier = Modifier.padding(32.dp).semantics { testTag = "favorites-no-results" },
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(8.dp),
) {
    Text(stringResource(R.string.favorites_no_results_title), style = MaterialTheme.typography.titleLarge)
    Text(stringResource(R.string.favorites_no_results), color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortBottomSheet(selected: FavoriteSortOrder, onSelected: (FavoriteSortOrder) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
            Text(stringResource(R.string.favorites_sort_title), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp))
            FavoriteSortOrder.entries.forEach { order ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onSelected(order) }.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = selected == order, onClick = { onSelected(order) })
                    Text(sortLabel(order), Modifier.padding(12.dp))
                }
            }
        }
    }
}

@Composable
private fun sortLabel(order: FavoriteSortOrder): String = stringResource(when (order) {
    FavoriteSortOrder.RecentlyAdded -> R.string.favorites_sort_recent
    FavoriteSortOrder.TitleAscending -> R.string.favorites_sort_title_ascending
    FavoriteSortOrder.RatingDescending -> R.string.favorites_sort_rating
    FavoriteSortOrder.ReleaseDateDescending -> R.string.favorites_sort_newest
    FavoriteSortOrder.ReleaseDateAscending -> R.string.favorites_sort_oldest
})

private fun favoriteMetadata(movie: FavoriteMovieUiModel): String = buildList {
    movie.releaseDate?.take(4)?.let(::add)
    movie.voteAverage?.takeIf { it > 0 }?.let { add("★ ${String.format(Locale.getDefault(), "%.1f", it)}") }
}.joinToString("  •  ").ifBlank { "—" }
