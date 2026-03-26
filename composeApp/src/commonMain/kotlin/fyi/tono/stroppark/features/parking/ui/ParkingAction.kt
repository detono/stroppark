package fyi.tono.stroppark.features.parking.ui

import fyi.tono.stroppark.features.parking.domain.ParkingFilter

sealed interface ParkingAction {
  data class ToggleFilter(val filter: ParkingFilter) : ParkingAction
  data object Refresh : ParkingAction
  data object RequestLocationPermission: ParkingAction
  data object DismissDialog: ParkingAction
}