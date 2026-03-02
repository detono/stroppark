package fyi.tono.stroppark.core.location

import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.location.LOCATION
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

class MokoLocationPermissionService(
  private val controller: PermissionsController
) : LocationPermissionService {

  override val state: StateFlow<LocationPermissionState> = flow {
    val permission = controller.getPermissionState(Permission.LOCATION)
    emit(permission.toLocationPermissionState())
  }.stateIn(CoroutineScope(Dispatchers.Main), SharingStarted.Eagerly, LocationPermissionState.NotDetermined)

  override suspend fun requestPermission() {
    try {
      controller.providePermission(Permission.LOCATION)
    } catch (e: DeniedException) {
      // state flow will update automatically
    } catch (e: DeniedAlwaysException) {
      // same
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