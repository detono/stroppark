package fyi.tono.stroppark.features.parking.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import fyi.tono.stroppark.core.location.LocationPermissionService
import fyi.tono.stroppark.core.location.LocationPermissionState
import fyi.tono.stroppark.core.location.LocationService
import fyi.tono.stroppark.core.location.LocationUtils
import fyi.tono.stroppark.core.utils.PermissionDialog
import fyi.tono.stroppark.features.parking.domain.ParkingRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
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

  private val _dialogDismissed = MutableStateFlow(false)
  val locationDialog = permissionState.combine(_dialogDismissed) { state, dismissed ->
    if (dismissed) return@combine PermissionDialog.None
    when (state) {
      LocationPermissionState.Denied -> PermissionDialog.Rationale
      LocationPermissionState.DeniedAlways -> PermissionDialog.Settings
      else -> PermissionDialog.None
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PermissionDialog.None)


  private var pollingJob: Job? = null

  init {
    val safeLocationFlow = permissionState.flatMapLatest { state ->
      if (state == LocationPermissionState.Granted) {
        locationService.getLocationFlow()
          .onStart { emit(locationService.getLastKnownLocation()) }
          .catch { emit(null) }
      } else {
        flowOf(null)
      }
    }

    combine(
      repository.getParkingFlow(),
      safeLocationFlow
    ) { spots, location ->
      location?.let { userLoc ->
        spots.map { spot ->
          val distance = if (spot.latitude != null && spot.longitude != null) {
            LocationUtils.calculateDistance(
              userLoc.lat, userLoc.lon,
              spot.latitude, spot.longitude
            )
          } else null
          spot.copy(distanceKm = distance)
        }.sortedBy { it.distanceKm ?: Double.MAX_VALUE }
      } ?: spots
    }.onEach { spots ->
      _uiState.update { currentState ->
        currentState.copy(
          parkingSpots = spots,
          isLoading = false
        )
      }
    }
    .launchIn(viewModelScope)

    permissionState
      .drop(1)
      .onEach { state ->
        if (state == LocationPermissionState.Granted) {
          _dialogDismissed.update { false }
        }
      }.launchIn(viewModelScope)
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
        _dialogDismissed.update { true }
        viewModelScope.launch {
          locationPermission.requestPermission()
        }
      }
      ParkingAction.DismissDialog -> _dialogDismissed.update { true }
    }
  }

  private fun startPolling() {
    pollingJob?.cancel()
    pollingJob = viewModelScope.launch {
      while (isActive) {
        if (!_uiState.value.isLoading) {
          fetchData(isSilent = _uiState.value.parkingSpots.isNotEmpty())
        }

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