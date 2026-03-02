package fyi.tono.stroppark.core.location

sealed interface LocationPermissionState {
  data object Granted : LocationPermissionState
  data object Denied : LocationPermissionState
  data object DeniedAlways : LocationPermissionState  // user said "never ask again"
  data object NotDetermined : LocationPermissionState
  data object NotGranted : LocationPermissionState
}