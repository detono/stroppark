package fyi.tono.stroppark.features.parking.ui

import fyi.tono.stroppark.features.parking.domain.ParkingLocation

data class ParkingUiState(
  val isLoading: Boolean = false,
  val parkingSpots: List<ParkingLocation> = emptyList(),
  val errorMessage: String? = null
)