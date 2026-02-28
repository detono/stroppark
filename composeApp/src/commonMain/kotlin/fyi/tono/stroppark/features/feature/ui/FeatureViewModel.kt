package fyi.tono.stroppark.features.feature.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FeatureViewModel : ViewModel() {
  private val _uiState = MutableStateFlow<FeatureUiState>(FeatureUiState.Loading)
  val uiState = _uiState.asStateFlow()

  init {
    TODO("Implement this")
  }
}