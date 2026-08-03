package com.example.tmdbmovies.feature.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.example.tmdbmovies.BuildConfig
import com.example.tmdbmovies.core.ui.theme.TmdbMoviesTheme

class ProfileFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            TmdbMoviesTheme {
                ProfileScreen(versionName = BuildConfig.VERSION_NAME)
            }
        }
    }
}
