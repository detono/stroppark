package fyi.tono.stroppark.core.location

import kotlinx.coroutines.flow.StateFlow

interface LocationPermissionService {
  val state: StateFlow<LocationPermissionState>
  suspend fun requestPermission()
}