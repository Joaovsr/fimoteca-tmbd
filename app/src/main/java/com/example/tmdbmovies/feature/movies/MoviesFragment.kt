package com.example.tmdbmovies.feature.movies

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.example.tmdbmovies.R
import com.example.tmdbmovies.core.ui.theme.TmdbMoviesTheme
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel

class MoviesFragment : Fragment() {
    private val viewModel: MoviesViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        val searchMode = arguments?.getBoolean("searchMode") == true
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            TmdbMoviesTheme {
                MoviesRoute(
                    viewModel = viewModel,
                    searchMode = searchMode,
                    onMovieClick = { movieId ->
                        findNavController().navigate(
                            if (searchMode) {
                                R.id.action_searchFragment_to_movieDetailsFragment
                            } else {
                                R.id.action_moviesFragment_to_movieDetailsFragment
                            },
                            Bundle().apply { putLong("movieId", movieId) },
                        )
                    },
                    onSearchClick = { findNavController().navigate(R.id.action_moviesFragment_to_searchFragment) },
                    onBackClick = { findNavController().navigateUp() },
                )
            }
        }
    }
}
