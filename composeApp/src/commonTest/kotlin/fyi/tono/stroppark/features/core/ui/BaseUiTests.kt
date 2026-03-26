package fyi.tono.stroppark.features.core.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import fyi.tono.stroppark.core.utils.LocalSnackbar

expect abstract class BaseUiTests()

@OptIn(ExperimentalTestApi::class)
fun ComposeUiTest.setContentWithSnackbar(content: @Composable () -> Unit) {
  setContent {
    val snackbarHostState = remember { SnackbarHostState() }
    CompositionLocalProvider(LocalSnackbar provides snackbarHostState) {
      content()
    }
  }
}