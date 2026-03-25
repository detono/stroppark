package fyi.tono.stroppark.features.chargers.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import fyi.tono.stroppark.core.location.LocationPermissionState
import fyi.tono.stroppark.features.chargers.ui.ChargerAction
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


  //region Rationale
  var showRationale by remember { mutableStateOf(false) }
  var showSettingsPrompt by remember { mutableStateOf(false) }
  val permissionState by viewModel.permissionState.collectAsState()
  LaunchedEffect(permissionState) {
    when (permissionState) {
      LocationPermissionState.Granted -> {
        viewModel.fetchData()
      }
      LocationPermissionState.DeniedAlways -> {
        showSettingsPrompt = true
      }
      LocationPermissionState.NotDetermined -> {
        viewModel.onAction(ChargerAction.RequestLocationPermission)
      }
      LocationPermissionState.Denied -> {
        showRationale = true
      }
      LocationPermissionState.NotGranted -> {
        viewModel.onAction(ChargerAction.RequestLocationPermission)
      }
    }
  }

  if (showRationale) {
    AlertDialog(
      onDismissRequest = { showRationale = false },
      title = { Text("Location needed") },
      text = { Text("We need your location to calculate distances to nearby chargers.") },
      confirmButton = {
        TextButton(onClick = {
          showRationale = false
          viewModel.onAction(ChargerAction.RequestLocationPermission)
        }) { Text("OK") }
      },
      dismissButton = {
        TextButton(onClick = { showRationale = false }) { Text("No thanks") }
      }
    )
  }

  if (showSettingsPrompt) {
    AlertDialog(
      onDismissRequest = { showSettingsPrompt = false },
      title = { Text("Permission required") },
      text = { Text("You've permanently denied location access. Enable it in Settings to see distances.") },
      confirmButton = {
        TextButton(onClick = {
          showSettingsPrompt = false

        }) { Text("Open Settings") }
      },
      dismissButton = {
        TextButton(onClick = { showSettingsPrompt = false }) { Text("Dismiss") }
      }
    )
  }
  //endregion

  PullToRefreshBox(
    modifier = Modifier.fillMaxSize(),
    isRefreshing = uiState.isLoading,
    onRefresh = {
      viewModel.fetchData(isSilent = false)
    },
    content = {
      when {
        uiState.isLoading && uiState.chargers.isEmpty() -> {
          uiState.syncProgress?.let {
            Box(
              modifier = Modifier.fillMaxSize(),
              contentAlignment = Alignment.Center,
              content = {
                Card(
                  modifier = Modifier.wrapContentSize(),
                  shape = RoundedCornerShape(16.dp),
                  elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                  colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                  ),
                  content = {
                    Column(
                      modifier = Modifier.padding(32.dp),
                      horizontalAlignment = Alignment.CenterHorizontally,
                      verticalArrangement = Arrangement.spacedBy(16.dp),
                      content = {
                        CircularProgressIndicator(
                          progress = { it.loaded.toFloat() / it.total.toFloat() },
                          color = ProgressIndicatorDefaults.circularColor,
                          strokeWidth = ProgressIndicatorDefaults.CircularStrokeWidth,
                          trackColor = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
                          strokeCap = ProgressIndicatorDefaults.CircularDeterminateStrokeCap,
                        )
                        Text(
                          text = "Synchronising ${it.loaded} of ${it.total} chargers",
                          style = MaterialTheme.typography.bodyMedium,
                          color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                      }
                    )
                  }
                )
              }
            )
          } ?: run {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
          }
        }

        uiState.errorMessage != null && uiState.chargers.isEmpty() -> {
          Text(uiState.errorMessage!!, modifier = Modifier.align(Alignment.Center))
        }

        else -> {
          ChargerList(
            uiState = uiState,
            onAction = viewModel::onAction
          )
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