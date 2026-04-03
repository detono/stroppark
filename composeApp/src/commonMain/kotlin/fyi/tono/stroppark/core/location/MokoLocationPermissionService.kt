package fyi.tono.stroppark.core.location

import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.location.LOCATION
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MokoLocationPermissionService(
  private val controller: PermissionsController
) : LocationPermissionService {
  private val _state = MutableStateFlow<LocationPermissionState>(LocationPermissionState.NotDetermined)
  override val state: StateFlow<LocationPermissionState> = _state

  init {
    CoroutineScope(Dispatchers.Main).launch {
      _state.value = controller.getPermissionState(Permission.LOCATION).toLocationPermissionState()
    }
  }

  override suspend fun requestPermission() {
    try {
      controller.providePermission(Permission.LOCATION)
    } catch (e: DeniedException) {
    } catch (e: DeniedAlwaysException) {
    } finally {
      // this runs after the user taps Allow or Deny
      _state.value = controller.getPermissionState(Permission.LOCATION).toLocationPermissionState()
    }
  }

  override fun refreshPermissionState() {
    CoroutineScope(Dispatchers.Main).launch {
      _state.value = controller.getPermissionState(Permission.LOCATION).toLocationPermissionState()
    }
  }
}

private fun PermissionState.toLocationPermissionState() = when (this) {
  PermissionState.Granted        -> LocationPermissionState.Granted
  PermissionState.Denied         -> LocationPermissionState.Denied
  PermissionState.DeniedAlways   -> LocationPermissionState.DeniedAlways
  PermissionState.NotDetermined  -> LocationPermissionState.NotDetermined
  PermissionState.NotGranted     -> LocationPermissionState.NotGranted
}