package fyi.tono.stroppark.features.parking.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fyi.tono.stroppark.features.parking.domain.ParkingRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ParkingViewModel(
  private val repository: ParkingRepository
): ViewModel() {
  private val _uiState = MutableStateFlow(ParkingUiState(isLoading = true))
  val uiState = _uiState.asStateFlow()

  private var pollingJob: Job? = null

  fun onLifecycleEvent(isForeground: Boolean) {
    if (isForeground) {
      startPolling()
    } else {
      pollingJob?.cancel()
    }
  }

  private fun startPolling() {
    pollingJob?.cancel()
    pollingJob = viewModelScope.launch {
      while (isActive) {
        fetchData(isSilent = _uiState.value.parkingSpots.isNotEmpty())
        delay(5 * 60 * 1000) // 5 Minutes
      }
    }
  }

  fun fetchData(isSilent: Boolean = false) {
    viewModelScope.launch {
      if (!isSilent) _uiState.update { it.copy(isLoading = true) }

      try {
        val spots = repository.getParkingOccupancy()
        _uiState.update {
          it.copy(
            parkingSpots = spots,
            isLoading = false,
            errorMessage = null
          )
        }
      } catch (e: Exception) {
        _uiState.update {
          it.copy(
            isLoading = false,
            errorMessage = "Could not update: ${e.message}"
          )
        }
      }
    }
  }
}