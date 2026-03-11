package fyi.tono.stroppark.features.map.ui

import fyi.tono.stroppark.features.map.domain.MapFilter
import fyi.tono.stroppark.features.map.domain.PoiType

sealed interface MapAction {
  data object RequestLocationPermission: MapAction
  data object LocationPermissionGranted: MapAction
  data object DismissMarker: MapAction
  data object FinishedLoading: MapAction
  data class ToggleFilter(val filter: MapFilter): MapAction
  data class SelectMarker(val id: String, val type: PoiType): MapAction
}