package fyi.tono.stroppark.features.chargers.ui

import androidx.lifecycle.ViewModel
import fyi.tono.stroppark.features.parking.ui.ParkingUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChargerViewModel: ViewModel() {
  private val _uiState = MutableStateFlow<ChargerUiState>(ChargerUiState.Loading)
  val uiState = _uiState.asStateFlow()
}