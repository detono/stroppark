package fyi.tono.stroppark.features.chargers.ui

import fyi.tono.stroppark.features.chargers.domain.ChargerPoint

sealed interface ChargerUiState {
  object Loading: ChargerUiState
  data class Success(val chargers: List<ChargerPoint>) : ChargerUiState
  data class Error(val message: String) : ChargerUiState
}