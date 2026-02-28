package fyi.tono.stroppark.features.parking.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ParkingViewModel: ViewModel() {
  private val _uiState = MutableStateFlow<ParkingUiState>(ParkingUiState.Loading)
  val uiState = _uiState.asStateFlow()
}