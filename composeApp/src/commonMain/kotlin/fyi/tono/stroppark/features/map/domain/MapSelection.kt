package fyi.tono.stroppark.features.map.domain

import fyi.tono.stroppark.features.chargers.ui.ChargerUiModel
import fyi.tono.stroppark.features.parking.domain.ParkingLocation

sealed interface MapSelection {
  data class Parking(val location: ParkingLocation) : MapSelection
  data class Charger(val charger: ChargerUiModel) : MapSelection
}