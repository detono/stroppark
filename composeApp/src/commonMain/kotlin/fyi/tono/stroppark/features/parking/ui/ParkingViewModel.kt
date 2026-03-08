package fyi.tono.stroppark.features.parking.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fyi.tono.stroppark.core.location.LocationPermissionService
import fyi.tono.stroppark.core.location.LocationPermissionState
import fyi.tono.stroppark.core.location.LocationService
import fyi.tono.stroppark.core.location.LocationUtils
import fyi.tono.stroppark.features.parking.domain.ParkingRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ParkingViewModel(
  private val repository: ParkingRepository,
  private val locationService: LocationService,
  private val locationPermission: LocationPermissionService
): ViewModel() {
  private val _uiState = MutableStateFlow(ParkingUiState(isLoading = true))
  val uiState = _uiState.asStateFlow()

  val permissionState = locationPermission.state
    .stateIn(
      viewModelScope,
      SharingStarted.Eagerly,
      LocationPermissionState.NotDetermined
    )

  private var pollingJob: Job? = null

  init {
    repository.getParkingFlow().onEach { spots ->
      println("Received ${spots.size} spots")
      _uiState.update { currentState ->
        currentState.copy(
          parkingSpots = spots, // Unsorted, no distances yet
          isLoading = false
        )
      }

      viewModelScope.launch {
        val userLoc = try {
          locationService.getCurrentLocation()
        } catch (e: Exception) {
          println("Failed to get location: ${e.message}")
          null
        }

        _uiState.update { currentState ->
          println("Trying to update ${spots.size} spots")
          println("Got current location ${userLoc?.lon}")

          val mappedSpots = spots.map { spot ->
            val distance = if (userLoc != null && spot.latitude != null && spot.longitude != null) {
              println("Calculating distance")
              LocationUtils.calculateDistance(
                userLoc.lat, userLoc.lon,
                spot.latitude, spot.longitude
              )
            } else null
            spot.copy(distanceKm = distance)
          }.sortedBy { it.distanceKm ?: Double.MAX_VALUE }
          println("Trying to update ${spots.size} spots after it was sorted")
          currentState.copy(
            parkingSpots = mappedSpots,
            isLoading = false
          )
        }
      }
    }
    .launchIn(viewModelScope)
  }

  fun onLifecycleEvent(isForeground: Boolean) {
    if (isForeground) {
      startPolling()
    } else {
      pollingJob?.cancel()
    }
  }

  fun onAction(action: ParkingAction) {
    when (action) {
      is ParkingAction.ToggleFilter -> {
        _uiState.update { current ->
          val newFilters = if (action.filter in current.activeFilters) {
            current.activeFilters - action.filter
          } else (
              current.activeFilters + action.filter
          )

          current.copy(activeFilters = newFilters)
        }
      }
      ParkingAction.Refresh -> fetchData()
      ParkingAction.RequestLocationPermission -> {
        viewModelScope.launch {
          locationPermission.requestPermission()
        }
      }
    }
  }

  private fun startPolling() {
    pollingJob?.cancel()
    pollingJob = viewModelScope.launch {
      while (isActive) {
        fetchData(isSilent = _uiState.value.parkingSpots.isNotEmpty())
        delay(15 * 60 * 1_000) // 15 Minutes
      }
    }
  }

  fun fetchData(isSilent: Boolean = false) {
    viewModelScope.launch {
      if (!isSilent) _uiState.update { it.copy(isLoading = true) }

      repository.refreshParkingOccupancy().fold(
        onSuccess = {
          _uiState.update {
            it.copy(
              isLoading = false,
              errorMessage = null
            )
          }
        },
        onFailure = { throwable ->
          _uiState.update { it.copy(
            isLoading = false,
            errorMessage = throwable.message ?: "An unexpected error occurred"
          )}
        }
      )
    }
  }
}