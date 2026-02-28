package fyi.tono.stroppark.features.chargers.ui

import fyi.tono.stroppark.features.chargers.domain.ChargerPoint
import fyi.tono.stroppark.features.parking.domain.ParkingLocation

data class ChargerUiState (
  val isLoading: Boolean = false,
  val chargers: List<ChargerPoint> = emptyList(),
  val errorMessage: String? = null,
)

