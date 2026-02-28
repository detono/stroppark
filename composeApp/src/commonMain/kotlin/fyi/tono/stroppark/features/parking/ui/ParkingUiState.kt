package fyi.tono.stroppark.features.parking.ui

import fyi.tono.stroppark.features.parking.domain.ParkingLocation

sealed interface ParkingUiState {
  object Loading: ParkingUiState
  data class Success(val parkingSpots: List<ParkingLocation>) : ParkingUiState
  data class Error(val message: String) : ParkingUiState
}