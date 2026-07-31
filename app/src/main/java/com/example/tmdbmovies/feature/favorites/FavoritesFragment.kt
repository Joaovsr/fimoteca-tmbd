package com.example.tmdbmovies.feature.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.example.tmdbmovies.R
import com.example.tmdbmovies.core.ui.theme.TmdbMoviesTheme
import androidx.navigation.fragment.findNavController
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavoritesFragment : Fragment() {
    private val viewModel: FavoritesViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            TmdbMoviesTheme {
                FavoritesScreen(
                    state = viewModel.state.collectAsStateWithLifecycle().value,
                    onMovieClick = { movieId ->
                        findNavController().navigate(
                            R.id.action_favoritesFragment_to_movieDetailsFragment,
                            Bundle().apply { putLong("movieId", movieId) },
                        )
                    },
                    onRemoveClick = viewModel::removeFavorite,
                    onBackClick = { findNavController().navigateUp() },
                )
            }
        }
    }
}
