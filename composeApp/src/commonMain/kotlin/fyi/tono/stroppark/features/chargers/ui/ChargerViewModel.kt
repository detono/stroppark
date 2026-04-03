package fyi.tono.stroppark.features.chargers.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import fyi.tono.stroppark.core.location.LocationPermissionService
import fyi.tono.stroppark.core.location.LocationPermissionState
import fyi.tono.stroppark.core.location.LocationService
import fyi.tono.stroppark.core.location.LocationUtils
import fyi.tono.stroppark.core.utils.PermissionDialog
import fyi.tono.stroppark.features.chargers.domain.ChargerRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
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
class ChargerViewModel(
  private val repository: ChargerRepository,
  private val locationService: LocationService,
  private val locationPermission: LocationPermissionService,
  private val logger: Logger = Logger.withTag("ChargerViewModel")
): ViewModel() {
  private val _uiState = MutableStateFlow(ChargerUiState(isLoading = true))
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
      logger.d { "Permission changed: $state" }

      if (state == LocationPermissionState.Granted) {
        logger.d { "Starting location flow" }

        locationService.getLocationFlow()
          .onStart {
            logger.d { "Emitting last known location" }

            emit(locationService.getLastKnownLocation())
          }
          .catch {
            logger.e(it) { "Location flow failed" }

            emit(null)
          }
      } else {
        logger.d { "Emitting null because not granted" }

        flowOf(null)
      }
    }

    combine(
      repository.getStationFlow(),
      safeLocationFlow
    ) { stations, location ->
      val chargers = stations.map { it.toUiModel() }

      location?.let { userLoc ->
        chargers.map { charger ->
          val distance = if (charger.latitude != null && charger.longitude != null) {
            LocationUtils.calculateDistance(
              userLoc.lat, userLoc.lon,
              charger.latitude, charger.longitude
            )
          } else null
          charger.copy(distanceKm = distance)
        }.sortedBy { it.distanceKm ?: Double.MAX_VALUE }
      } ?: chargers

    }.onEach { chargers ->
      _uiState.update { it.copy(chargers = chargers, isLoading = false) }
    }.launchIn(viewModelScope)

    permissionState
      .drop(1)
      .onEach { state ->
      if (state == LocationPermissionState.Granted) {
        _dialogDismissed.update { false }
      }
    }.launchIn(viewModelScope)
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
        _dialogDismissed.update { true }
        viewModelScope.launch {
          locationPermission.requestPermission()
        }
      }

      ChargerAction.DismissDialog -> _dialogDismissed.update { true }
    }
  }

  fun onLifecycleEvent(isForeground: Boolean) {
    if (isForeground) {
      locationPermission.refreshPermissionState()
      startPolling()
    } else {
      pollingJob?.cancel()
    }
  }

  private fun startPolling() {
    pollingJob?.cancel()
    pollingJob = viewModelScope.launch {
      while (isActive) {
        if (!_uiState.value.isLoading) {
          fetchData(isSilent = _uiState.value.chargers.isNotEmpty())
        }

        delay(60 * 60 * 1000 * 24) // 24 Hours
      }
    }
  }

  fun fetchData(isSilent: Boolean = false) = viewModelScope.launch {
    if (!isSilent) _uiState.update { it.copy(isLoading = true) }

    repository.refreshStations().onEach { progress ->
      if (progress.done) {
        _uiState.update { it.copy(isLoading = false, syncProgress = null, errorMessage = null) }
      } else {
        _uiState.update { it.copy(syncProgress = progress) }
      }
    }
    .catch { e ->
        _uiState.update { it.copy(isLoading = false, errorMessage = "Could not update: ${e.message}") }
    }
    .collect()
  }
}