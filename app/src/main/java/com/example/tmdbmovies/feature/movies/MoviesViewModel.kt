package com.example.tmdbmovies.feature.movies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.example.tmdbmovies.domain.model.MovieFilters
import com.example.tmdbmovies.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MoviesViewModel(
    repository: MovieRepository,
) : ViewModel() {
    val movies: Flow<PagingData<MovieUiModel>> = repository
        .pagedMovies(query = "", filters = MovieFilters())
        .map { pagingData -> pagingData.map { it.toUiModel() } }
        .cachedIn(viewModelScope)
}
