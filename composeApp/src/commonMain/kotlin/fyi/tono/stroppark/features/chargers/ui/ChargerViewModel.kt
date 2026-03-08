package fyi.tono.stroppark.features.chargers.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fyi.tono.stroppark.core.location.LocationPermissionService
import fyi.tono.stroppark.core.location.LocationPermissionState
import fyi.tono.stroppark.core.location.LocationService
import fyi.tono.stroppark.core.location.LocationUtils
import fyi.tono.stroppark.features.chargers.domain.ChargerRepository
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

class ChargerViewModel(
  private val repository: ChargerRepository,
  private val locationService: LocationService,
  private val locationPermission: LocationPermissionService
): ViewModel() {
  private val _uiState = MutableStateFlow(ChargerUiState(isLoading = true))
  val uiState = _uiState.asStateFlow()

  val permissionState = locationPermission.state
    .stateIn(
      viewModelScope,
      SharingStarted.Eagerly,
      LocationPermissionState.NotDetermined
    )

  private var pollingJob: Job? = null

  init {
    repository.getStationFlow().onEach { stationsAndConnectors ->
      val chargers = stationsAndConnectors.map { it.toUiModel() }
      _uiState.update { currentState ->
        currentState.copy(
          chargers = chargers,
          isLoading = false
        )
      }

      viewModelScope.launch {
        val userLoc = try {
          locationService.getCurrentLocation()
        } catch (e: Exception) {
          null
        }

        _uiState.update { currentState ->
          val mappedChargers = chargers.map { charger ->
            val distance = if (userLoc != null && charger.latitude != null && charger.longitude != null) {
              LocationUtils.calculateDistance(
                userLoc.lat, userLoc.lon,
                charger.latitude, charger.longitude
              )
            } else null
            charger.copy(distanceKm = distance)
          }.sortedBy { it.distanceKm ?: Double.MAX_VALUE }

          currentState.copy(
            chargers = mappedChargers,
            isLoading = false
          )
        }
      }
    }
      .launchIn(viewModelScope)
  }

  fun onAction(action: ChargerAction) {
    when (action) {
      is ChargerAction.ToggleFilter -> {
        _uiState.update { current ->
          val newFilters = if (action.filter in current.activeFilters) {
            current.activeFilters - action.filter
          } else (
              current.activeFilters + action.filter
              )

          current.copy(activeFilters = newFilters)
        }
      }
      ChargerAction.Refresh -> fetchData()
      ChargerAction.RequestLocationPermission -> {
        viewModelScope.launch {
          locationPermission.requestPermission()
        }
      }
    }
  }

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
        fetchData(isSilent = _uiState.value.chargers.isNotEmpty())
        delay(5 * 60 * 1000) // 5 Minutes
      }
    }
  }

  fun fetchData(isSilent: Boolean = false) {
    viewModelScope.launch {
      if (!isSilent) _uiState.update { it.copy(isLoading = true) }

      try {
        repository.refreshStations().fold(
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