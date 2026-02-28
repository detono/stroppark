package fyi.tono.stroppark.features.chargers.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import fyi.tono.stroppark.features.chargers.ui.ChargerViewModel
import fyi.tono.stroppark.features.chargers.ui.components.ChargerList
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChargerListScreen(viewModel: ChargerViewModel = koinViewModel()) {
  val uiState by viewModel.uiState.collectAsState()

  LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
    viewModel.onLifecycleEvent(isForeground = true)
  }
  LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
    viewModel.onLifecycleEvent(isForeground = false)
  }

  LaunchedEffect(Unit) {
    viewModel.fetchData()
  }

  PullToRefreshBox(
    modifier = Modifier.fillMaxSize(),
    isRefreshing = uiState.isLoading,
    onRefresh = {
      viewModel.fetchData(isSilent = false)
    },
    content = {
      when {
        uiState.isLoading && uiState.chargers.isEmpty() -> {
          CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        uiState.errorMessage != null && uiState.chargers.isEmpty() -> {
          Text(uiState.errorMessage!!, modifier = Modifier.align(Alignment.Center))
        }

        else -> {
          ChargerList(uiState.chargers)
        }
      }

      if (uiState.errorMessage != null && uiState.chargers.isNotEmpty()) {
        Surface(
          color = MaterialTheme.colorScheme.errorContainer,
          modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
        ) {
          Text(
            text = uiState.errorMessage!!,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(8.dp),
            textAlign = TextAlign.Center
          )
        }
      }
    }
  )
}