package fyi.tono.stroppark.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
  primary = GhentPurple,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFE1E0FF),
  secondary = GhentCyan,
  onSecondary = Color.White,
  surface = Color(0xFFF8F9FA), // Very light grey for a clean look
  onSurface = GhentBlack,
  error = Color(0xFFBA1A1A),
  errorContainer = Color(0xFFFFDAD6)
)

private val DarkColorScheme = darkColorScheme(
  primary = md_theme_dark_primary,
  onPrimary = md_theme_dark_onPrimary,
  secondary = md_theme_dark_secondary,
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