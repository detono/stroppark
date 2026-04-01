package fyi.tono.stroppark.features.map.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import eu.buney.maps.LatLngBounds
import fyi.tono.stroppark.core.location.LocationPermissionService
import fyi.tono.stroppark.core.location.LocationPermissionState
import fyi.tono.stroppark.core.location.LocationService
import fyi.tono.stroppark.core.network.dto.GhentCoordinatesDto
import fyi.tono.stroppark.features.chargers.domain.ChargerRepository
import fyi.tono.stroppark.features.chargers.ui.toUiModel
import fyi.tono.stroppark.features.map.domain.MapFilter
import fyi.tono.stroppark.features.map.domain.MapMarker
import fyi.tono.stroppark.features.map.domain.MapSelection
import fyi.tono.stroppark.features.map.domain.PoiType
import fyi.tono.stroppark.features.parking.domain.ParkingRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class MapViewModel(
  private val parkingRepository: ParkingRepository,
  private val chargerRepository: ChargerRepository,
  private val locationService: LocationService,
  private val locationPermission: LocationPermissionService,
  private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
  private val logger: Logger = Logger.withTag("MapViewModel")
) : ViewModel() {
  private val _uiState = MutableStateFlow(MapUiState())
  val uiState = _uiState.asStateFlow()

  private val _visibleBounds = MutableStateFlow<LatLngBounds?>(null)
  val filteredMarkers = combine(
    chargerRepository.getStationFlow(),
    parkingRepository.getParkingFlow(),
    _visibleBounds.debounce(500),
    uiState.map { it.activeFilters }.distinctUntilChanged()
  ) { chargers, parking, bounds, filters ->
    val allPossibleMarkers = mutableListOf<MapMarker>()

    bounds?.let {
      if (filters.contains(MapFilter.CHARGERS)) {
        allPossibleMarkers.addAll(
          chargers.filter {
            it.station.latitude in bounds.southwest.latitude..bounds.northeast.latitude &&
              it.station.longitude in bounds.southwest.longitude..bounds.northeast.longitude
          }.map { it.toMarker() }
        )
      }
      if (filters.contains(MapFilter.PARKING)) {
        allPossibleMarkers.addAll(
          parking.filter {
            it.latitude != null && it.longitude != null &&
            it.latitude in bounds.southwest.latitude..bounds.northeast.latitude &&
              it.longitude in bounds.southwest.longitude..bounds.northeast.longitude
          }.map { it.toMarker() }
        )
      }
    }

    Triple(chargers, parking, allPossibleMarkers)
  }
    .flowOn(defaultDispatcher)
    .onEach { (chargers, parking, markers) ->
      _uiState.update {
        it.copy(
          chargers = chargers.map { it.toUiModel() },
          parking = parking,
          markers = markers
        )
      }
    }
    .launchIn(viewModelScope)

  val permissionState = locationPermission.state
    .stateIn(
      viewModelScope,
      SharingStarted.Eagerly,
      LocationPermissionState.NotDetermined
    )

  private var pollingJob: Job? = null

  @OptIn(ExperimentalCoroutinesApi::class)
  val safeLocationFlow = permissionState.flatMapLatest { state ->
    if (state == LocationPermissionState.Granted) {
      locationService.getLocationFlow()
        .onStart { emit(locationService.getLastKnownLocation() ?: GhentCoordinatesDto(
          51.0543,
          3.7174
        )
        ) }
        .catch { emit(null) }
    } else {
      flowOf(null)
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
        logger.i("Selected ${action.id} of type ${action.type}")
        if (action.type == PoiType.PARKING) {
          _uiState.value.parking.find { it.id == action.id }?.let { selected ->
            _uiState.update { it.copy(mapSelection = MapSelection.Parking(selected)) }
          }
        }
        if (action.type == PoiType.CHARGER) {
          _uiState.value.chargers.find { it.id.toString() == action.id }?.let { selected ->
            _uiState.update { it.copy(mapSelection = MapSelection.Charger(selected)) }
          } ?: run {
            logger.i("Could not find ${action.type} with id ${action.id} - ${_uiState.value.chargers.size}")
          }
        }
      }

      MapAction.FinishedLoading -> {
        _uiState.update { it.copy(isLoading = false) }
      }

      is MapAction.UpdateBounds -> {
        _visibleBounds.update { action.bounds }
      }
    }
  }

  private fun startPolling() {
    pollingJob?.cancel()

    pollingJob = viewModelScope.launch(Dispatchers.IO) {
      safeLocationFlow.onEach { location ->
        location?.let {
          _uiState.update { curState ->
            curState.copy(currentLocation = it)
          }
        }
      }.launchIn(this)
    }
  }
}