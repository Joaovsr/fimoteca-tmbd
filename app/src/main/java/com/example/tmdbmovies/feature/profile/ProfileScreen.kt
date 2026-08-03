package com.example.tmdbmovies.feature.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.tmdbmovies.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(versionName: String) {
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.profile_title),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            ProfileSection(
                title = stringResource(R.string.profile_collection_title),
                body = stringResource(R.string.profile_collection_description),
            )
            ProfileSection(
                title = stringResource(R.string.profile_about_title),
                body = stringResource(R.string.profile_version, versionName),
            )
            ProfileSection(
                title = stringResource(R.string.profile_credits_title),
                body = stringResource(R.string.tmdb_attribution),
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ProfileSection(title: String, body: String) {
    Card(Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.semantics { heading() },
            )
            HorizontalDivider()
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
