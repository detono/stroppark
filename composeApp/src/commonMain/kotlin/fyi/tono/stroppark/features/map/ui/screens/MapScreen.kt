package fyi.tono.stroppark.features.map.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import fyi.tono.stroppark.core.location.LocationPermissionState
import fyi.tono.stroppark.features.map.ui.MapAction
import fyi.tono.stroppark.features.map.ui.MapViewModel
import fyi.tono.stroppark.features.map.ui.components.MapScreenContent
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(viewModel: MapViewModel = koinViewModel()) {
  val uiState by viewModel.uiState.collectAsState()

  LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
    viewModel.onLifecycleEvent(isForeground = true)
  }
  LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
    viewModel.onLifecycleEvent(isForeground = false)
  }

  //region Rationale
  var showRationale by remember { mutableStateOf(false) }
  var showSettingsPrompt by remember { mutableStateOf(false) }
  val permissionState by viewModel.permissionState.collectAsState()
  LaunchedEffect(permissionState) {
    when (permissionState) {
      LocationPermissionState.Granted -> {
        viewModel.onAction(MapAction.LocationPermissionGranted)
      }
      LocationPermissionState.DeniedAlways -> {
        showSettingsPrompt = true
      }
      LocationPermissionState.NotDetermined -> {
        viewModel.onAction(MapAction.RequestLocationPermission)
      }
      LocationPermissionState.Denied -> {
        showRationale = true
      }
      LocationPermissionState.NotGranted -> {
        viewModel.onAction(MapAction.RequestLocationPermission)
      }
    }
  }

  if (showRationale) {
    AlertDialog(
      onDismissRequest = { showRationale = false },
      title = { Text("Location needed") },
      text = { Text("We need your location to calculate distances to nearby parking.") },
      confirmButton = {
        TextButton(onClick = {
          showRationale = false
          viewModel.onAction(MapAction.RequestLocationPermission)
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

  MapScreenContent(
    modifier = Modifier.fillMaxSize(),
    uiState = uiState,
    onAction = viewModel::onAction
  )
}