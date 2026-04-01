package fyi.tono.stroppark.core.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import fyi.tono.stroppark.core.location.LocationPermissionState
import fyi.tono.stroppark.core.ui.components.organisms.LocationPermissionDialog
import org.jetbrains.compose.resources.StringResource
import stroppark.composeapp.generated.resources.Res
import stroppark.composeapp.generated.resources.app_dismiss
import stroppark.composeapp.generated.resources.app_no_thx
import stroppark.composeapp.generated.resources.app_ok
import stroppark.composeapp.generated.resources.app_open_settings

@Composable
fun LocationPermissionHandler(
  permissionState: LocationPermissionState,
  locationDialog: PermissionDialog,
  onRequestPermission: () -> Unit,
  onDismissDialog: () -> Unit,
  onOpenSettings: () -> Unit,
  rationaleTitle: StringResource,
  rationaleText: StringResource,
  settingsTitle: StringResource,
  settingsText: StringResource,
) {
  LaunchedEffect(permissionState) {
    if (permissionState == LocationPermissionState.NotDetermined ||
      permissionState == LocationPermissionState.NotGranted) {
      onRequestPermission()
    }
  }

  when (locationDialog) {
    PermissionDialog.Rationale -> {
      LocationPermissionDialog(
        title = rationaleTitle,
        text = rationaleText,
        confirmText = Res.string.app_ok,
        dismissText = Res.string.app_no_thx,
        onConfirm = onRequestPermission,
        onDismiss = onDismissDialog
      )
    }
    PermissionDialog.Settings -> {
      LocationPermissionDialog(
        title = settingsTitle,
        text = settingsText,
        confirmText = Res.string.app_open_settings,
        dismissText = Res.string.app_dismiss,
        onConfirm = onOpenSettings,
        onDismiss = onDismissDialog
      )
    }
    PermissionDialog.None -> Unit
  }
}