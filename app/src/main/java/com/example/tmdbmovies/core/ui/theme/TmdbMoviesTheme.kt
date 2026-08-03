package com.example.tmdbmovies.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val MovieBackground = Color(0xFF0B0D0E)
val MovieCardSurface = Color(0xFF151819)
val MovieSecondarySurface = Color(0xFF202425)
val MovieAccent = Color(0xFF65D46E)
val MovieTextPrimary = Color(0xFFF4F6F5)
val MovieTextSecondary = Color(0xFFA7AEAA)
val MovieDivider = Color(0xFF2A2E2F)
val MovieRating = Color(0xFFFFC94A)

private val MovieColorScheme: ColorScheme = darkColorScheme(
    primary = MovieAccent,
    onPrimary = MovieBackground,
    primaryContainer = MovieSecondarySurface,
    onPrimaryContainer = MovieAccent,
    secondary = MovieAccent,
    onSecondary = MovieBackground,
    background = MovieBackground,
    onBackground = MovieTextPrimary,
    surface = MovieCardSurface,
    onSurface = MovieTextPrimary,
    surfaceVariant = MovieSecondarySurface,
    onSurfaceVariant = MovieTextSecondary,
    outline = MovieDivider,
)

private val MovieTypography = Typography(
    headlineLarge = TextStyle(fontSize = 30.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold),
    headlineMedium = TextStyle(fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 21.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold),
    titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontSize = 12.sp, lineHeight = 17.sp),
)

private val MovieShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(24.dp),
)

@Composable
fun TmdbMoviesTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = MovieColorScheme,
        typography = MovieTypography,
        shapes = MovieShapes,
        content = content,
    )
}
