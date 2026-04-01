package fyi.tono.stroppark.features.map.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import fyi.tono.stroppark.core.utils.LocationPermissionHandler
import fyi.tono.stroppark.core.utils.openAppSettings
import fyi.tono.stroppark.features.map.ui.MapAction
import fyi.tono.stroppark.features.map.ui.MapUiState
import fyi.tono.stroppark.features.map.ui.MapViewModel
import fyi.tono.stroppark.features.map.ui.components.MapScreenContent
import org.koin.compose.viewmodel.koinViewModel
import stroppark.composeapp.generated.resources.Res
import stroppark.composeapp.generated.resources.location_permission_title
import stroppark.composeapp.generated.resources.location_permission_title_settings
import stroppark.composeapp.generated.resources.map_location_permission_perm_denied
import stroppark.composeapp.generated.resources.map_location_permission_text

typealias MapContent = @Composable (uiState: MapUiState, onAction: (MapAction) -> Unit, modifier: Modifier) -> Unit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
  viewModel: MapViewModel = koinViewModel(),
  mapContent: MapContent = { uiState, onAction, modifier ->
    MapScreenContent(
      modifier = modifier,
      uiState = uiState,
      onAction = onAction
    )
  }
) {
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
      viewModel.onAction(MapAction.RequestLocationPermission)
    },
    onDismissDialog = {
      viewModel.onAction(MapAction.DismissDialog)
    },
    onOpenSettings = {
      viewModel.onAction(MapAction.DismissDialog)
      openAppSettings()
    },
    rationaleTitle = Res.string.location_permission_title,
    rationaleText = Res.string.map_location_permission_text,
    settingsTitle = Res.string.location_permission_title_settings,
    settingsText = Res.string.map_location_permission_perm_denied
  )

  mapContent(
    uiState,
    viewModel::onAction,
    Modifier.fillMaxSize()
  )
}