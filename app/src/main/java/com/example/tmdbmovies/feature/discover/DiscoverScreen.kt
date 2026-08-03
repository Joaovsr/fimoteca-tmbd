package com.example.tmdbmovies.feature.discover

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.tmdbmovies.R
import com.example.tmdbmovies.core.ui.theme.MovieAccent
import com.example.tmdbmovies.core.ui.tmdbBackdropUrl
import com.example.tmdbmovies.core.ui.tmdbPosterUrl
import com.example.tmdbmovies.domain.model.MovieCollection
import com.example.tmdbmovies.feature.movies.MovieUiModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    state: DiscoverUiState,
    onMovieClick: (Long) -> Unit,
    onFavoriteClick: (MovieUiModel) -> Unit,
    onRetry: (MovieCollection) -> Unit,
    onFeaturedSelected: (Int) -> Unit,
    onSearchClick: () -> Unit,
) {
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium) },
                actions = {
                    IconButton(
                        onClick = onSearchClick,
                        modifier = Modifier.semantics { testTag = "open-search" },
                    ) {
                        Icon(painterResource(R.drawable.ic_search), stringResource(R.string.search_title))
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            item {
                FeaturedContent(
                    section = state.sections.getValue(MovieCollection.TrendingWeekly),
                    featured = state.featuredMovie,
                    featuredMovies = state.featuredMovies,
                    selectedIndex = state.selectedFeaturedIndex,
                    onMovieClick = onMovieClick,
                    onFavoriteClick = onFavoriteClick,
                    onRetry = { onRetry(MovieCollection.TrendingWeekly) },
                    onSelected = onFeaturedSelected,
                )
            }
            MovieCollection.entries.forEach { collection ->
                item(key = collection.name) {
                    MovieSection(
                        collection = collection,
                        state = state.sections.getValue(collection),
                        onMovieClick = onMovieClick,
                        onFavoriteClick = onFavoriteClick,
                        onRetry = { onRetry(collection) },
                    )
                }
            }
        }
    }
}

@Composable
private fun FeaturedContent(
    section: DiscoverSectionState,
    featured: MovieUiModel?,
    featuredMovies: List<MovieUiModel>,
    selectedIndex: Int,
    onMovieClick: (Long) -> Unit,
    onFavoriteClick: (MovieUiModel) -> Unit,
    onRetry: () -> Unit,
    onSelected: (Int) -> Unit,
) {
    when {
        featured != null -> FeaturedMovieBanner(featured, featuredMovies.size, selectedIndex, onMovieClick, onFavoriteClick, onSelected)
        section.isLoading -> FeaturedPlaceholder()
        section.errorMessageRes != null -> SectionMessage(section.errorMessageRes, onRetry, Modifier.padding(horizontal = 16.dp).height(220.dp))
        else -> SectionEmpty(Modifier.padding(horizontal = 16.dp).height(180.dp))
    }
}

@Composable
private fun FeaturedMovieBanner(
    movie: MovieUiModel,
    pageCount: Int,
    selectedIndex: Int,
    onMovieClick: (Long) -> Unit,
    onFavoriteClick: (MovieUiModel) -> Unit,
    onSelected: (Int) -> Unit,
) {
    val title = movie.title.ifBlank { stringResource(R.string.movie_title_unavailable) }
    Card(
        onClick = { onMovieClick(movie.id) },
        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().height(272.dp).semantics { testTag = "featured-movie" },
        shape = RoundedCornerShape(14.dp),
    ) {
        Box(Modifier.fillMaxSize()) {
            AsyncImage(
                model = tmdbBackdropUrl(movie.backdropPath),
                contentDescription = null,
                placeholder = painterResource(R.drawable.ic_movie_fallback),
                error = painterResource(R.drawable.ic_movie_fallback),
                fallback = painterResource(R.drawable.ic_movie_fallback),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            Box(
                Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = .88f)), startY = 55f),
                ),
            )
            FavoriteIcon(movie, onFavoriteClick, Modifier.align(Alignment.TopEnd).padding(8.dp))
            Column(
                modifier = Modifier.align(Alignment.BottomStart).padding(16.dp).fillMaxWidth(.78f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Surface(color = MovieAccent, shape = RoundedCornerShape(12.dp)) {
                    Text(stringResource(R.string.discover_badge_trending), Modifier.padding(horizontal = 10.dp, vertical = 4.dp), color = Color.Black, style = MaterialTheme.typography.labelSmall)
                }
                Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(metadata(movie), style = MaterialTheme.typography.bodySmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                    repeat(pageCount) { index ->
                        val selected = index == selectedIndex
                        Box(
                            Modifier.size(if (selected) 10.dp else 8.dp).clip(CircleShape)
                                .background(if (selected) MovieAccent else Color.White.copy(alpha = .55f))
                                .clickable { onSelected(index) }
                                .semantics { contentDescription = "${index + 1} de $pageCount" },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MovieSection(
    collection: MovieCollection,
    state: DiscoverSectionState,
    onMovieClick: (Long) -> Unit,
    onFavoriteClick: (MovieUiModel) -> Unit,
    onRetry: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.semantics { testTag = "section-${collection.name}" }) {
        Text(sectionTitle(collection), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 16.dp))
        when {
            state.movies.isNotEmpty() -> LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) { items(state.movies, key = MovieUiModel::id) { MoviePosterCard(it, collection, onMovieClick, onFavoriteClick) } }
            state.isLoading -> LoadingRow()
            state.errorMessageRes != null -> SectionMessage(state.errorMessageRes, onRetry, Modifier.padding(horizontal = 16.dp).height(170.dp))
            else -> SectionEmpty(Modifier.padding(horizontal = 16.dp).height(120.dp))
        }
    }
}

@Composable
private fun MoviePosterCard(movie: MovieUiModel, collection: MovieCollection, onMovieClick: (Long) -> Unit, onFavoriteClick: (MovieUiModel) -> Unit) {
    val title = movie.title.ifBlank { stringResource(R.string.movie_title_unavailable) }
    Column(Modifier.width(132.dp).clickable { onMovieClick(movie.id) }.semantics { testTag = "discover-movie-${movie.id}" }, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.fillMaxWidth().aspectRatio(2f / 3f).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
            AsyncImage(
                model = tmdbPosterUrl(movie.posterPath),
                contentDescription = stringResource(R.string.movie_poster_content_description, title),
                placeholder = painterResource(R.drawable.ic_movie_fallback), error = painterResource(R.drawable.ic_movie_fallback), fallback = painterResource(R.drawable.ic_movie_fallback),
                contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize(),
            )
            FavoriteIcon(movie, onFavoriteClick, Modifier.align(Alignment.TopEnd).padding(6.dp))
            if (collection == MovieCollection.Classics) movie.releaseDate?.take(4)?.toIntOrNull()?.let { year ->
                Surface(Modifier.align(Alignment.BottomStart).padding(6.dp), color = Color.Black.copy(alpha = .72f), shape = RoundedCornerShape(10.dp)) {
                    Text(decadeLabel(year), Modifier.padding(horizontal = 7.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        Text(title, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis, minLines = 2)
        Text(metadata(movie), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
    }
}

@Composable
private fun FavoriteIcon(movie: MovieUiModel, onFavoriteClick: (MovieUiModel) -> Unit, modifier: Modifier = Modifier) {
    val title = movie.title.ifBlank { stringResource(R.string.movie_title_unavailable) }
    Box(modifier = modifier.size(48.dp), contentAlignment = Alignment.Center) {
        Surface(modifier = Modifier.size(40.dp), shape = CircleShape, color = Color.Black.copy(alpha = .68f)) {}
        IconButton(
            onClick = { onFavoriteClick(movie) },
            modifier = Modifier.fillMaxSize().semantics { testTag = "discover-favorite-${movie.id}" },
        ) {
            Icon(
                painterResource(if (movie.isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_outline),
                stringResource(if (movie.isFavorite) R.string.favorite_remove_description else R.string.favorite_save_description, title),
                tint = if (movie.isFavorite) MovieAccent else Color.White,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun LoadingRow() = LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
    items(3) { Box(Modifier.width(132.dp).aspectRatio(2f / 3f).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) { CircularProgressIndicator(Modifier.size(28.dp)) } }
}

@Composable
private fun FeaturedPlaceholder() = Box(Modifier.padding(horizontal = 16.dp).fillMaxWidth().height(272.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.surfaceVariant), Alignment.Center) { CircularProgressIndicator() }

@Composable
private fun SectionMessage(messageRes: Int, onRetry: () -> Unit, modifier: Modifier) = Box(modifier.fillMaxWidth(), Alignment.Center) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(messageRes), style = MaterialTheme.typography.bodyMedium)
        TextButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
    }
}

@Composable
private fun SectionEmpty(modifier: Modifier) = Box(modifier.fillMaxWidth(), Alignment.Center) { Text(stringResource(R.string.discover_section_empty), color = MaterialTheme.colorScheme.onSurfaceVariant) }

@Composable
private fun sectionTitle(collection: MovieCollection): String = stringResource(when (collection) {
    MovieCollection.TrendingWeekly -> R.string.discover_trending
    MovieCollection.NowPlaying -> R.string.discover_now_playing
    MovieCollection.Classics -> R.string.discover_classics
    MovieCollection.TopRated -> R.string.discover_top_rated
})

private fun metadata(movie: MovieUiModel): String = buildList {
    movie.releaseDate?.take(4)?.let(::add)
    movie.voteAverage?.takeIf { it > 0.0 }?.let { add("★ ${String.format(Locale.getDefault(), "%.1f", it)}") }
}.joinToString("  •  ").ifBlank { "—" }

private fun decadeLabel(year: Int): String = "ANOS ${(year / 10) * 10}"
