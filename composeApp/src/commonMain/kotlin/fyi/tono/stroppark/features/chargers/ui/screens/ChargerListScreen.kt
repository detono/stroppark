package fyi.tono.stroppark.features.chargers.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import fyi.tono.stroppark.core.utils.LocationPermissionHandler
import fyi.tono.stroppark.core.utils.openAppSettings
import fyi.tono.stroppark.features.chargers.ui.ChargerAction
import fyi.tono.stroppark.features.chargers.ui.ChargerTestTags
import fyi.tono.stroppark.features.chargers.ui.ChargerViewModel
import fyi.tono.stroppark.features.chargers.ui.components.ChargerList
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import stroppark.composeapp.generated.resources.Res
import stroppark.composeapp.generated.resources.charger_location_permission_perm_denied
import stroppark.composeapp.generated.resources.charger_location_permission_text
import stroppark.composeapp.generated.resources.charger_sync_finalizing
import stroppark.composeapp.generated.resources.charger_sync_progress
import stroppark.composeapp.generated.resources.location_permission_title
import stroppark.composeapp.generated.resources.location_permission_title_settings

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

  val permissionState by viewModel.permissionState.collectAsState()
  val locationDialog by viewModel.locationDialog.collectAsState()
  LocationPermissionHandler(
    permissionState = permissionState,
    locationDialog = locationDialog,
    onRequestPermission = {
      viewModel.onAction(ChargerAction.RequestLocationPermission)
    },
    onDismissDialog = {
      viewModel.onAction(ChargerAction.DismissDialog)
    },
    onOpenSettings = {
      viewModel.onAction(ChargerAction.DismissDialog)
      openAppSettings()
    },
    rationaleTitle = Res.string.location_permission_title,
    rationaleText = Res.string.charger_location_permission_text,
    settingsTitle = Res.string.location_permission_title_settings,
    settingsText = Res.string.charger_location_permission_perm_denied
  )

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
              modifier = Modifier.fillMaxSize().testTag(ChargerTestTags.SYNC_PROGRESS_CARD),
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
                        val text = if (it.loaded == it.total) {
                          stringResource(resource = Res.string.charger_sync_finalizing)
                        } else {
                          stringResource(
                            resource = Res.string.charger_sync_progress,
                            formatArgs = arrayOf(it.loaded, it.total)
                          )
                        }

                        CircularProgressIndicator(
                          modifier = Modifier.testTag(ChargerTestTags.LOADING_SPINNER),
                          progress = { it.loaded.toFloat() / it.total.toFloat() },
                          color = ProgressIndicatorDefaults.circularColor,
                          strokeWidth = ProgressIndicatorDefaults.CircularStrokeWidth,
                          trackColor = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
                          strokeCap = ProgressIndicatorDefaults.CircularDeterminateStrokeCap,
                        )
                        Text(
                          text = text,
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
            CircularProgressIndicator(
              modifier = Modifier.align(Alignment.Center).testTag(ChargerTestTags.LOADING_SPINNER)
            )
          }
        }

        uiState.errorMessage != null && uiState.chargers.isEmpty() -> {
          Text(uiState.errorMessage!!, modifier = Modifier.align(Alignment.Center))
        }

        else -> {
          ChargerList(
            modifier = Modifier.testTag(ChargerTestTags.CHARGER_LIST),
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
            .testTag(ChargerTestTags.ERROR_BANNER)
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