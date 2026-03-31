package fyi.tono.stroppark.features.map.ui

import fyi.tono.stroppark.core.location.LocationPermissionState
import fyi.tono.stroppark.core.network.dto.GhentCoordinatesDto
import fyi.tono.stroppark.features.chargers.ui.ChargerUiModel
import fyi.tono.stroppark.features.map.domain.MapFilter
import fyi.tono.stroppark.features.map.domain.MapMarker
import fyi.tono.stroppark.features.map.domain.MapSelection
import fyi.tono.stroppark.features.map.domain.PoiType
import fyi.tono.stroppark.features.parking.domain.ParkingLocation

data class MapUiState(
  val chargers: List<ChargerUiModel> = emptyList(),
  val parking: List<ParkingLocation> = emptyList(),
  val currentLocation: GhentCoordinatesDto = DefaultLocation,
  val currentZoom: Float = 10f,
  val locationPermissionState: LocationPermissionState? = null,
  val isLoading: Boolean = true,
  val activeFilters: Set<MapFilter> = setOf(MapFilter.PARKING, MapFilter.CHARGERS),
  val mapSelection: MapSelection? = null,
  val markers: List<MapMarker> = emptyList()
) {
  companion object {
    val DefaultLocation = GhentCoordinatesDto(lat = 51.0543422, lon = 3.7174243)
  }
}