package com.example.tmdbmovies.feature.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import coil3.load
import coil3.request.error
import coil3.request.fallback
import coil3.request.placeholder
import com.example.tmdbmovies.R
import com.example.tmdbmovies.core.ui.formatReleaseDate
import com.example.tmdbmovies.core.ui.tmdbBackdropUrl
import com.example.tmdbmovies.databinding.FragmentMovieDetailsBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class MovieDetailsFragment : Fragment() {
    private var binding: FragmentMovieDetailsBinding? = null
    private val viewModel: MovieDetailsViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = FragmentMovieDetailsBinding.inflate(inflater, container, false)
        .also { binding = it }
        .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = requireNotNull(binding)
        binding.detailsToolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.retryButton.setOnClickListener { viewModel.retry() }
        binding.favoriteButton.setOnClickListener { viewModel.onFavoriteClick() }
        viewModel.state.observe(viewLifecycleOwner, ::render)
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    private fun render(state: MovieDetailsUiState) {
        val binding = binding ?: return
        binding.loadingIndicator.isVisible = state is MovieDetailsUiState.Loading
        binding.errorContainer.isVisible = state is MovieDetailsUiState.Error
        binding.contentContainer.isVisible = state is MovieDetailsUiState.Content

        when (state) {
            MovieDetailsUiState.Loading -> Unit
            is MovieDetailsUiState.Error -> binding.errorMessage.setText(state.messageRes)
            is MovieDetailsUiState.Content -> renderContent(binding, state.movie, state.isFavorite)
        }
    }

    private fun renderContent(
        binding: FragmentMovieDetailsBinding,
        movie: MovieDetailsUiModel,
        isFavorite: Boolean,
    ) {
        val title = movie.title ?: getString(R.string.movie_title_unavailable)
        binding.movieTitle.text = title
        binding.movieOverview.text = movie.overview ?: getString(R.string.movie_overview_unavailable)
        binding.movieReleaseDate.text = formatReleaseDate(movie.releaseDate)?.let {
            getString(R.string.details_release_date, it)
        } ?: getString(R.string.movie_date_unavailable)
        binding.movieRating.isVisible = movie.voteAverage != null
        binding.movieRating.text = movie.voteAverage?.let { getString(R.string.details_rating, it) }
        binding.favoriteButton.setText(if (isFavorite) R.string.favorite_remove else R.string.favorite_save)
        binding.favoriteButton.setIconResource(
            if (isFavorite) R.drawable.ic_favorite_filled else R.drawable.ic_favorite_outline,
        )
        binding.favoriteButton.contentDescription = getString(
            if (isFavorite) R.string.favorite_remove_description else R.string.favorite_save_description,
            title,
        )
        binding.movieBackdrop.load(tmdbBackdropUrl(movie.backdropPath)) {
            placeholder(R.drawable.ic_movie_fallback)
            error(R.drawable.ic_movie_fallback)
            fallback(R.drawable.ic_movie_fallback)
        }
    }
}
