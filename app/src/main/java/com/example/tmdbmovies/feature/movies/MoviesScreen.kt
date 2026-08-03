package com.example.tmdbmovies.feature.movies

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import coil3.compose.AsyncImage
import com.example.tmdbmovies.R
import com.example.tmdbmovies.core.common.AppError
import com.example.tmdbmovies.core.common.AppErrorException
import com.example.tmdbmovies.core.ui.tmdbPosterUrl
import com.example.tmdbmovies.domain.model.Genre
import com.example.tmdbmovies.domain.model.MovieFilters
import com.example.tmdbmovies.domain.model.MovieSortOrder
import com.example.tmdbmovies.domain.model.compatibleWithQuery

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoviesScreen(
    movies: LazyPagingItems<MovieUiModel>,
    uiState: MoviesUiState = MoviesUiState(),
    onQueryChanged: (String) -> Unit = {},
    onFiltersChanged: (MovieFilters) -> Unit = {},
    onClearFilters: () -> Unit = {},
    onRetryGenres: () -> Unit = {},
    onMovieClick: (Long) -> Unit,
    onFavoriteClick: (MovieUiModel) -> Unit = {},
    searchMode: Boolean = true,
    onSearchClick: () -> Unit = {},
    onBackClick: () -> Unit = {},
) {
    val listState = rememberLazyListState()
    val requestKey = uiState.requestKey()
    var previousRequestKey by rememberSaveable { mutableStateOf(requestKey) }
    LaunchedEffect(requestKey) {
        if (previousRequestKey != requestKey) {
            listState.scrollToItem(0)
            previousRequestKey = requestKey
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(if (searchMode) R.string.search_title else R.string.app_name),
                        style = MaterialTheme.typography.headlineLarge,
                    )
                },
                navigationIcon = {
                    if (searchMode) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                painterResource(R.drawable.ic_arrow_back),
                                contentDescription = stringResource(R.string.back),
                            )
                        }
                    }
                },
                actions = {
                    if (!searchMode) {
                        IconButton(
                            onClick = onSearchClick,
                            modifier = Modifier.semantics { testTag = "open-search" },
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_search),
                                contentDescription = stringResource(R.string.search_title),
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            MoviesControls(
                uiState = uiState,
                showSearchField = searchMode,
                onQueryChanged = onQueryChanged,
                onFiltersChanged = onFiltersChanged,
                onClearFilters = onClearFilters,
                onRetryGenres = onRetryGenres,
            )
            Box(Modifier.fillMaxWidth().weight(1f)) {
                when (val refresh = movies.loadState.refresh) {
                    is LoadState.Loading if movies.itemCount == 0 -> LoadingContent()
                    is LoadState.Error if movies.itemCount == 0 -> MoviesErrorContent(
                        message = errorMessage(refresh.error),
                        onRetry = movies::retry,
                    )
                    is LoadState.NotLoading if movies.itemCount == 0 -> EmptyContent(uiState.isSearching)
                    else -> MoviesListContent(movies, listState, refresh, onMovieClick, onFavoriteClick)
                }
            }
        }
    }
}

@Composable
private fun MoviesControls(
    uiState: MoviesUiState,
    showSearchField: Boolean = true,
    onQueryChanged: (String) -> Unit,
    onFiltersChanged: (MovieFilters) -> Unit,
    onClearFilters: () -> Unit,
    onRetryGenres: () -> Unit,
) {
    var showFilters by rememberSaveable { mutableStateOf(false) }
    val visibleFilters = uiState.filters.compatibleWithQuery(uiState.query)
    val hasVisibleFilters = visibleFilters != MovieFilters()

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (showSearchField) {
            OutlinedTextField(
                value = uiState.query,
                onValueChange = onQueryChanged,
                singleLine = true,
                label = { Text(stringResource(R.string.movies_search_label)) },
                placeholder = { Text(stringResource(R.string.movies_search_hint)) },
                leadingIcon = {
                    Icon(
                        painterResource(R.drawable.ic_search),
                        contentDescription = null,
                    )
                },
                trailingIcon = {
                    if (uiState.query.isNotEmpty()) {
                        IconButton(
                            onClick = { onQueryChanged("") },
                            modifier = Modifier.semantics { testTag = "search-clear" },
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_close),
                                contentDescription = stringResource(R.string.clear_search),
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().semantics { testTag = "movies-search" },
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            FilledTonalButton(
                onClick = { showFilters = true },
                modifier = Modifier.semantics { testTag = "filters-open" },
            ) {
                Icon(painterResource(R.drawable.ic_sort), contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.filters))
            }
            if (hasVisibleFilters) {
                TextButton(onClick = onClearFilters) { Text(stringResource(R.string.filters_clear_all)) }
            }
            if (uiState.isLoadingGenres) {
                val loadingGenres = stringResource(R.string.genres_loading)
                CircularProgressIndicator(
                    Modifier.size(20.dp).semantics { contentDescription = loadingGenres },
                )
            }
        }
        ActiveFilters(
            filters = uiState.filters,
            query = uiState.query,
            genres = uiState.genres,
            onFiltersChanged = onFiltersChanged,
        )
        uiState.genresErrorMessageRes?.let { messageRes ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(messageRes), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                TextButton(onClick = onRetryGenres) { Text(stringResource(R.string.retry)) }
            }
        }
        if (uiState.isSearching && uiState.filters.hasDiscoverOnlyFilters()) {
            Text(
                stringResource(R.string.filters_search_policy),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    if (showFilters) {
        FiltersDialog(
            filters = uiState.filters,
            genres = uiState.genres,
            query = uiState.query,
            onApply = {
                onFiltersChanged(it)
                showFilters = false
            },
            onClear = {
                onClearFilters()
                showFilters = false
            },
            onDismiss = { showFilters = false },
        )
    }
}

@Composable
private fun ActiveFilters(
    filters: MovieFilters,
    query: String,
    genres: List<Genre>,
    onFiltersChanged: (MovieFilters) -> Unit,
) {
    val visibleFilters = filters.compatibleWithQuery(query)
    if (visibleFilters == MovieFilters()) return
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        visibleFilters.genreId?.let { genreId ->
            val label = genres.firstOrNull { it.genreId == genreId }?.name ?: stringResource(R.string.filter_genre)
            FilterChip(
                selected = true,
                onClick = { onFiltersChanged(filters.copy(genreId = null)) },
                label = { Text(label) },
            )
        }
        if (visibleFilters.sortOrder != MovieSortOrder.PopularityDescending) {
            FilterChip(
                selected = true,
                onClick = { onFiltersChanged(filters.copy(sortOrder = MovieSortOrder.PopularityDescending)) },
                label = { Text(sortOrderLabel(visibleFilters.sortOrder)) },
            )
        }
        visibleFilters.minimumRating?.let { rating ->
            FilterChip(
                selected = true,
                onClick = { onFiltersChanged(filters.copy(minimumRating = null)) },
                label = { Text(stringResource(R.string.filter_rating_value, rating)) },
            )
        }
        visibleFilters.releaseYear?.let { year ->
            FilterChip(
                selected = true,
                onClick = { onFiltersChanged(filters.copy(releaseYear = null)) },
                label = { Text(stringResource(R.string.filter_year_value, year)) },
            )
        }
    }
}

@Composable
private fun FiltersDialog(
    filters: MovieFilters,
    genres: List<Genre>,
    query: String,
    onApply: (MovieFilters) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
) {
    val isSearching = query.isNotBlank()
    var draft by remember(filters, query) { mutableStateOf(filters.compatibleWithQuery(query)) }
    var yearText by remember(filters.releaseYear) { mutableStateOf(filters.releaseYear?.toString().orEmpty()) }
    val parsedYear = yearText.takeIf(String::isNotBlank)?.toIntOrNull()
    val yearIsValid = yearText.isBlank() || parsedYear in 1000..9999

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.filters_title)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().heightIn(max = 480.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (isSearching) {
                    Text(stringResource(R.string.filters_search_only_year))
                } else {
                    GenreSelector(draft.genreId, genres) { draft = draft.copy(genreId = it) }
                    SortSelector(draft.sortOrder) { draft = draft.copy(sortOrder = it) }
                    Text(stringResource(R.string.filter_minimum_rating), style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(null, 5.0, 7.0).forEach { rating ->
                            FilterChip(
                                selected = draft.minimumRating == rating,
                                onClick = { draft = draft.copy(minimumRating = rating) },
                                label = {
                                    Text(rating?.let { stringResource(R.string.filter_rating_value, it) }
                                        ?: stringResource(R.string.filter_any))
                                },
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = yearText,
                    onValueChange = { value -> yearText = value.filter(Char::isDigit).take(4) },
                    label = { Text(stringResource(R.string.filter_year)) },
                    singleLine = true,
                    isError = !yearIsValid,
                    supportingText = if (yearIsValid) null else {
                        { Text(stringResource(R.string.filter_year_error)) }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth().semantics { testTag = "filter-year" },
                )
            }
        },
        confirmButton = {
            Button(
                enabled = yearIsValid,
                onClick = {
                    val applied = if (isSearching) {
                        filters.copy(releaseYear = parsedYear)
                    } else {
                        draft.copy(releaseYear = parsedYear)
                    }
                    onApply(applied)
                },
                modifier = Modifier.semantics { testTag = "filters-apply" },
            ) { Text(stringResource(R.string.apply)) }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onClear) { Text(stringResource(R.string.filters_clear_all)) }
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
            }
        },
    )
}

@Composable
private fun GenreSelector(selectedId: Long?, genres: List<Genre>, onSelected: (Long?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = genres.firstOrNull { it.genreId == selectedId }?.name ?: stringResource(R.string.filter_any)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(stringResource(R.string.filter_genre), style = MaterialTheme.typography.labelLarge)
        Box(Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) { Text(selectedName) }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.filter_any)) },
                    onClick = { onSelected(null); expanded = false },
                )
                genres.forEach { genre ->
                    DropdownMenuItem(text = { Text(genre.name) }, onClick = { onSelected(genre.genreId); expanded = false })
                }
            }
        }
    }
}

@Composable
private fun SortSelector(selected: MovieSortOrder, onSelected: (MovieSortOrder) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(stringResource(R.string.filter_sort), style = MaterialTheme.typography.labelLarge)
        Box(Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) { Text(sortOrderLabel(selected)) }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                MovieSortOrder.entries.forEach { sortOrder ->
                    DropdownMenuItem(
                        text = { Text(sortOrderLabel(sortOrder)) },
                        onClick = { onSelected(sortOrder); expanded = false },
                    )
                }
            }
        }
    }
}

@Composable
private fun sortOrderLabel(sortOrder: MovieSortOrder): String = stringResource(
    when (sortOrder) {
        MovieSortOrder.PopularityDescending -> R.string.filter_sort_popularity
        MovieSortOrder.ReleaseDateDescending -> R.string.filter_sort_release_date
        MovieSortOrder.VoteAverageDescending -> R.string.filter_sort_rating
    },
)

private fun MovieFilters.hasDiscoverOnlyFilters(): Boolean =
    genreId != null || sortOrder != MovieSortOrder.PopularityDescending || minimumRating != null

private fun MoviesUiState.requestKey(): String {
    val activeFilters = filters.compatibleWithQuery(query)
    return "${query.trim()}|${activeFilters.genreId}|${activeFilters.sortOrder}|${activeFilters.minimumRating}|${activeFilters.releaseYear}"
}

@Composable
private fun MoviesListContent(
    movies: LazyPagingItems<MovieUiModel>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    refresh: LoadState,
    onMovieClick: (Long) -> Unit,
    onFavoriteClick: (MovieUiModel) -> Unit,
) = LazyColumn(
    state = listState,
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
    modifier = Modifier.fillMaxSize(),
) {
    if (refresh is LoadState.Loading) item { RefreshLoading() }
    if (refresh is LoadState.Error) item {
        AppendError(message = errorMessage(refresh.error), onRetry = movies::retry)
    }
    items(count = movies.itemCount, key = movies.itemKey(MovieUiModel::id)) { index ->
        movies[index]?.let { movie ->
            MovieCard(
                movie = movie,
                onClick = { onMovieClick(movie.id) },
                onFavoriteClick = { onFavoriteClick(movie) },
            )
        }
    }
    when (val append = movies.loadState.append) {
        is LoadState.Loading -> item { AppendLoading() }
        is LoadState.Error -> item { AppendError(message = errorMessage(append.error), onRetry = movies::retry) }
        is LoadState.NotLoading -> Unit
    }
}

@Composable
private fun MovieCard(movie: MovieUiModel, onClick: () -> Unit, onFavoriteClick: () -> Unit) {
    val title = movie.title.ifBlank { stringResource(R.string.movie_title_unavailable) }
    val favoriteDescription = stringResource(
        if (movie.isFavorite) R.string.favorite_remove_description else R.string.favorite_save_description,
        title,
    )
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().semantics { testTag = "movie-card-${movie.id}" },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = tmdbPosterUrl(movie.posterPath),
                contentDescription = stringResource(R.string.movie_poster_content_description, title),
                placeholder = painterResource(R.drawable.ic_movie_fallback),
                error = painterResource(R.drawable.ic_movie_fallback),
                fallback = painterResource(R.drawable.ic_movie_fallback),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(width = 112.dp, height = 168.dp)
                    .clip(RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp))
                    .then(
                    if (movie.posterPath.isNullOrBlank()) Modifier.semantics { testTag = "movie-poster-fallback" }
                    else Modifier,
                ),
            )
            Column(
                modifier = Modifier.weight(1f).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                Text(
                    movie.releaseDate ?: stringResource(R.string.movie_date_unavailable),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                movie.voteAverage?.let { rating ->
                    Text(
                        stringResource(R.string.details_rating, rating),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
                TextButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier.semantics {
                        testTag = "movie-favorite-${movie.id}"
                        contentDescription = favoriteDescription
                    },
                ) {
                    Icon(
                        painterResource(
                            if (movie.isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_outline,
                        ),
                        contentDescription = null,
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(stringResource(if (movie.isFavorite) R.string.favorite_remove else R.string.favorite_save))
                }
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
internal fun EmptyContent(isSearching: Boolean = false, modifier: Modifier = Modifier) = Box(
    modifier = modifier.fillMaxSize().semantics { testTag = "movies-empty" },
    contentAlignment = Alignment.Center,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(24.dp),
    ) {
        Text(
            stringResource(if (isSearching) R.string.movies_search_empty_title else R.string.movies_empty_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            stringResource(if (isSearching) R.string.movies_search_empty else R.string.movies_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
internal fun MoviesErrorContent(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) = Box(
    modifier = modifier.fillMaxSize(),
    contentAlignment = Alignment.Center,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
        Text(stringResource(R.string.movies_error_title), style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(20.dp))
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
