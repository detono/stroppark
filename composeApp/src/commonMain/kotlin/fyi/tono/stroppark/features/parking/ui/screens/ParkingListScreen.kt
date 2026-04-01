package fyi.tono.stroppark.features.parking.ui.screens

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import fyi.tono.stroppark.core.utils.LocationPermissionHandler
import fyi.tono.stroppark.core.utils.openAppSettings
import fyi.tono.stroppark.features.parking.ui.ParkingAction
import fyi.tono.stroppark.features.parking.ui.ParkingViewModel
import org.koin.compose.viewmodel.koinViewModel
import stroppark.composeapp.generated.resources.Res
import stroppark.composeapp.generated.resources.location_permission_title
import stroppark.composeapp.generated.resources.location_permission_title_settings
import stroppark.composeapp.generated.resources.parking_location_permission_text

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

  val permissionState by viewModel.permissionState.collectAsState()
  val locationDialog by viewModel.locationDialog.collectAsState()
  LocationPermissionHandler(
    permissionState = permissionState,
    locationDialog = locationDialog,
    onRequestPermission = {
      viewModel.onAction(ParkingAction.RequestLocationPermission)
    },
    onDismissDialog = {
      viewModel.onAction(ParkingAction.DismissDialog)
    },
    onOpenSettings = {
      viewModel.onAction(ParkingAction.DismissDialog)
      openAppSettings()
    },
    rationaleTitle = Res.string.location_permission_title,
    rationaleText = Res.string.parking_location_permission_text,
    settingsTitle = Res.string.location_permission_title_settings,
    settingsText = Res.string.parking_location_permission_text
  )

  ParkingListScreenContent(
    uiState = uiState,
    onAction = viewModel::onAction
  )
}

