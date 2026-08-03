package com.example.tmdbmovies.feature.discover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.tmdbmovies.R
import com.example.tmdbmovies.core.ui.theme.TmdbMoviesTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

class DiscoverFragment : Fragment() {
    private val viewModel: DiscoverViewModel by viewModel()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                TmdbMoviesTheme {
                    DiscoverRoute(
                        viewModel = viewModel,
                        onMovieClick = { movieId ->
                            findNavController().navigate(
                                R.id.action_moviesFragment_to_movieDetailsFragment,
                                Bundle().apply { putLong("movieId", movieId) },
                            )
                        },
                        onSearchClick = { findNavController().navigate(R.id.action_moviesFragment_to_searchFragment) },
                    )
                }
            }
        }
}
