package fyi.tono.stroppark.features.parking.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import fyi.tono.stroppark.features.parking.ui.ParkingAction
import fyi.tono.stroppark.features.parking.ui.ParkingViewModel
import fyi.tono.stroppark.features.parking.ui.components.ParkingList
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkingListScreen(viewModel: ParkingViewModel = koinViewModel ()) {
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
        viewModel.onAction(ParkingAction.RequestLocationPermission)
      }
      LocationPermissionState.Denied -> {
        showRationale = true
      }
      LocationPermissionState.NotGranted -> {
        viewModel.onAction(ParkingAction.RequestLocationPermission)
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
          viewModel.onAction(ParkingAction.RequestLocationPermission)
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
      viewModel.onAction(ParkingAction.Refresh)
    },
    content = {
      when {
        uiState.isLoading && uiState.parkingSpots.isEmpty() -> {
          CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }

        uiState.errorMessage != null && uiState.parkingSpots.isEmpty() -> {
          Text(uiState.errorMessage!!, modifier = Modifier.align(Alignment.Center))
        }

        else -> {
          ParkingList(
            uiState = uiState,
            onAction = viewModel::onAction,
          )
        }
      }

      if (uiState.errorMessage != null && uiState.parkingSpots.isNotEmpty()) {
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
