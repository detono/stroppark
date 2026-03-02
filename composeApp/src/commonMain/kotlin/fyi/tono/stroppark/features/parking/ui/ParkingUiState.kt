package fyi.tono.stroppark.features.parking.ui

import fyi.tono.stroppark.features.parking.domain.ParkingFilter
import fyi.tono.stroppark.features.parking.domain.ParkingLocation

data class ParkingUiState(
  val isLoading: Boolean = false,
  val parkingSpots: List<ParkingLocation> = emptyList(),
  val errorMessage: String? = null,
  val activeFilters: Set<ParkingFilter> = setOf()
) {
  val availableFilters: Set<ParkingFilter> = buildSet {
    if (parkingSpots.any { !it.open }) add(ParkingFilter.AVAILABLE)
    if (parkingSpots.any { it.free }) add(ParkingFilter.FREE)
    if (parkingSpots.any { it.lez }) {
      add(ParkingFilter.AVOID_LEZ)
      add(ParkingFilter.LEZ)
    }
  }
}