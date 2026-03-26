package fyi.tono.stroppark.features.parking.ui.screens

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import fyi.tono.stroppark.core.ui.components.organisms.LocationPermissionDialog
import fyi.tono.stroppark.core.utils.PermissionDialog
import fyi.tono.stroppark.core.utils.openAppSettings
import fyi.tono.stroppark.features.parking.ui.ParkingAction
import fyi.tono.stroppark.features.parking.ui.ParkingViewModel
import org.koin.compose.viewmodel.koinViewModel
import stroppark.composeapp.generated.resources.Res
import stroppark.composeapp.generated.resources.app_dismiss
import stroppark.composeapp.generated.resources.app_no_thx
import stroppark.composeapp.generated.resources.app_ok
import stroppark.composeapp.generated.resources.app_open_settings
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

  val locationDialog by viewModel.locationDialog.collectAsState()
  when (locationDialog) {
    PermissionDialog.Rationale -> {
      LocationPermissionDialog(
        title = Res.string.location_permission_title,
        text = Res.string.parking_location_permission_text,
        confirmText = Res.string.app_ok,
        dismissText = Res.string.app_no_thx,
        onConfirm = {
          viewModel.onAction(ParkingAction.RequestLocationPermission)
        },
        onDismiss = { viewModel.onAction(ParkingAction.DismissDialog) }
      )
    }
    PermissionDialog.Settings -> {
      LocationPermissionDialog(
        title = Res.string.location_permission_title_settings,
        text = Res.string.parking_location_permission_text,
        confirmText = Res.string.app_open_settings,
        dismissText = Res.string.app_dismiss,
        onConfirm = {
          viewModel.onAction(ParkingAction.DismissDialog)
          openAppSettings()
        },
        onDismiss = { viewModel.onAction(ParkingAction.DismissDialog) }
      )
    }
    PermissionDialog.None -> Unit
  }

  ParkingListScreenContent(
    uiState = uiState,
    onAction = viewModel::onAction
  )
}

