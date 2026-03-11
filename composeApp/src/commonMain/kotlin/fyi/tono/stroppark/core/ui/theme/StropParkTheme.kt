package fyi.tono.stroppark.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
  primary = md_theme_light_primary,
  onPrimary = md_theme_light_onPrimary,
  primaryContainer = md_theme_light_primary_container,
  secondary = md_theme_light_secondary,
  onSecondary = Color.White,
  secondaryContainer = md_theme_light_secondary_container,
  onSecondaryContainer = md_theme_light_onSecondary_container,
  surface = md_theme_light_surface,
  onSurface = GhentBlack,
  error = md_theme_light_error,
  errorContainer = md_theme_light_error_container
)

private val DarkColorScheme = darkColorScheme(
  primary = md_theme_dark_primary,
  onPrimary = md_theme_dark_onPrimary,
  primaryContainer = md_theme_dark_primary_container,
  onPrimaryContainer = md_theme_dark_onPrimary_container,
  secondary = md_theme_dark_secondary,
  onSecondary = md_theme_dark_onSecondary,
  secondaryContainer = md_theme_dark_secondary_container,
  onSecondaryContainer = md_theme_dark_onSecondary_container,
  surface = GhentBlack,
  onSurface = Color.White
)

@Composable
fun StropParkTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    content = content
  )
}