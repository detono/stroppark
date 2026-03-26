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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import fyi.tono.stroppark.core.ui.components.organisms.LocationPermissionDialog
import fyi.tono.stroppark.core.utils.PermissionDialog
import fyi.tono.stroppark.core.utils.openAppSettings
import fyi.tono.stroppark.features.chargers.ui.ChargerAction
import fyi.tono.stroppark.features.chargers.ui.ChargerViewModel
import fyi.tono.stroppark.features.chargers.ui.components.ChargerList
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import stroppark.composeapp.generated.resources.Res
import stroppark.composeapp.generated.resources.app_dismiss
import stroppark.composeapp.generated.resources.app_no_thx
import stroppark.composeapp.generated.resources.app_ok
import stroppark.composeapp.generated.resources.app_open_settings
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

  val locationDialog by viewModel.locationDialog.collectAsState()
  when (locationDialog) {
    PermissionDialog.Rationale -> {
      LocationPermissionDialog(
        title = Res.string.location_permission_title,
        text = Res.string.charger_location_permission_text,
        confirmText = Res.string.app_ok,
        dismissText = Res.string.app_no_thx,
        onConfirm = {
          viewModel.onAction(ChargerAction.RequestLocationPermission)
        },
        onDismiss = { viewModel.onAction(ChargerAction.DismissDialog) }
      )
    }

    PermissionDialog.Settings -> {
      LocationPermissionDialog(
        title = Res.string.location_permission_title_settings,
        text = Res.string.charger_location_permission_perm_denied,
        confirmText = Res.string.app_open_settings,
        dismissText = Res.string.app_dismiss,
        onConfirm = {
          viewModel.onAction(ChargerAction.DismissDialog)
          openAppSettings()
        },
        onDismiss = { viewModel.onAction(ChargerAction.DismissDialog) }
      )
    }
    PermissionDialog.None -> Unit
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
                        val text = if (it.loaded == it.total) {
                          stringResource(resource = Res.string.charger_sync_finalizing)
                        } else {
                          stringResource(
                            resource = Res.string.charger_sync_progress,
                            formatArgs = arrayOf(it.loaded, it.total)
                          )
                        }

                        CircularProgressIndicator(
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