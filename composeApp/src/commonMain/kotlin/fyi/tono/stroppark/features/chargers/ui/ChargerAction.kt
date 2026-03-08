package fyi.tono.stroppark.features.chargers.ui

import fyi.tono.stroppark.features.chargers.domain.ChargerFilter

sealed interface ChargerAction {
  data class ToggleFilter(val filter: ChargerFilter) : ChargerAction
  data object Refresh : ChargerAction
  data object RequestLocationPermission: ChargerAction
}
