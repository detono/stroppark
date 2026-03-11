package fyi.tono.stroppark.features.map.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MapViewModel : ViewModel() {
  private val _uiState = MutableStateFlow<MapUiState>(MapUiState.Loading)
  val uiState = _uiState.asStateFlow()

  init {
    TODO("Implement this")
  }
}