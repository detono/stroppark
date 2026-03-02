package fyi.tono.stroppark.fakes

import fyi.tono.stroppark.core.location.LocationPermissionService
import fyi.tono.stroppark.core.location.LocationPermissionState
import kotlinx.coroutines.flow.MutableStateFlow


class FakeLocationPermissionService : LocationPermissionService {
  override val state = MutableStateFlow<LocationPermissionState>(LocationPermissionState.NotDetermined)
  override suspend fun requestPermission() {}
}