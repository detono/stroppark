package fyi.tono.stroppark.features.map.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fyi.tono.stroppark.core.location.LocationPermissionService
import fyi.tono.stroppark.core.location.LocationPermissionState
import fyi.tono.stroppark.core.location.LocationService
import fyi.tono.stroppark.features.chargers.domain.ChargerRepository
import fyi.tono.stroppark.features.chargers.ui.toUiModel
import fyi.tono.stroppark.features.map.domain.MapSelection
import fyi.tono.stroppark.features.map.domain.PoiType
import fyi.tono.stroppark.features.parking.domain.ParkingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class MapViewModel(
  private val parkingRepository: ParkingRepository,
  private val chargerRepository: ChargerRepository,
  private val locationService: LocationService,
  private val locationPermission: LocationPermissionService
) : ViewModel() {
  private val _uiState = MutableStateFlow(MapUiState())
  val uiState = _uiState.asStateFlow()

  val permissionState = locationPermission.state
    .stateIn(
      viewModelScope,
      SharingStarted.Eagerly,
      LocationPermissionState.NotDetermined
    )

  private var pollingJob: Job? = null

  init {
    viewModelScope.launch(Dispatchers.IO) {
      chargerRepository.refreshStations()
      withTimeoutOrNull(5_000L) {
        chargerRepository.getStationFlow()
          .first { it.isNotEmpty() }
      }?.let { chargers ->
        _uiState.update { cs -> cs.copy(chargers = chargers.map { it.toUiModel() }) }
      }
    }
    viewModelScope.launch(Dispatchers.IO) {
      parkingRepository.refreshParkingOccupancy()
      withTimeoutOrNull(5_000L) {
        parkingRepository.getParkingFlow()
          .first { it.isNotEmpty() }
      }?.let { spots ->
        _uiState.update { it.copy(parking = spots) }
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

  fun onAction(action: MapAction) {
    when (action) {
      MapAction.LocationPermissionGranted -> {
        _uiState.update { it.copy(locationPermissionState = LocationPermissionState.Granted) }
      }

      MapAction.RequestLocationPermission -> {
        viewModelScope.launch {
          locationPermission.requestPermission()
        }
      }

      MapAction.DismissMarker -> {
        _uiState.update { it.copy(mapSelection = null) }
      }

      is MapAction.ToggleFilter -> {
        _uiState.update { current ->
          val newFilters = if (action.filter in current.activeFilters) {
            current.activeFilters - action.filter
          } else (
              current.activeFilters + action.filter
              )

          current.copy(activeFilters = newFilters)
        }
      }

      is MapAction.SelectMarker -> {
        if (action.type == PoiType.PARKING) {
          _uiState.value.parking.find { it.id == action.id }?.let { selected ->
            _uiState.update { it.copy(mapSelection = MapSelection.Parking(selected)) }
          }
        }
        if (action.type == PoiType.CHARGER) {
          _uiState.value.chargers.find { it.id.toString() == action.id }?.let { selected ->
            _uiState.update { it.copy(mapSelection = MapSelection.Charger(selected)) }
          }
        }
      }

      MapAction.FinishedLoading -> {
        _uiState.update { it.copy(isLoading = false) }
      }
    }
  }

  private fun startPolling() {
    pollingJob?.cancel()

    pollingJob = viewModelScope.launch(Dispatchers.IO) {
      locationService.getLocationFlow()
        .onEach { location ->
          location?.let {
            _uiState.update { curState ->
              curState.copy(currentLocation = it)
            }
          }
        }
        .launchIn(this)
    }
  }
}