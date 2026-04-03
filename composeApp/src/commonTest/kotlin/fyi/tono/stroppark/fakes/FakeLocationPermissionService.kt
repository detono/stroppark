package fyi.tono.stroppark.fakes

import fyi.tono.stroppark.core.location.LocationPermissionService
import fyi.tono.stroppark.core.location.LocationPermissionState
import kotlinx.coroutines.flow.MutableStateFlow


class FakeLocationPermissionService : LocationPermissionService {
  var wasRequestCalled = false
    private set

  override val state = MutableStateFlow<LocationPermissionState>(LocationPermissionState.NotDetermined)
  override suspend fun requestPermission() {
    wasRequestCalled = true
  }

  override fun refreshPermissionState() {

  }
}