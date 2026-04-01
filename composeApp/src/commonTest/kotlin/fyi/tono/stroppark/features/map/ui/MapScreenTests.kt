package fyi.tono.stroppark.features.map.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import fyi.tono.stroppark.core.location.LocationPermissionState
import fyi.tono.stroppark.fakes.FakeChargerRepository
import fyi.tono.stroppark.fakes.FakeLocationPermissionService
import fyi.tono.stroppark.fakes.FakeLocationService
import fyi.tono.stroppark.fakes.FakeParkingRepository
import fyi.tono.stroppark.features.chargers.database.StationEntity
import fyi.tono.stroppark.features.chargers.database.StationWithConnectors
import fyi.tono.stroppark.features.chargers.ui.ChargerUiModel
import fyi.tono.stroppark.features.core.ui.BaseUiTests
import fyi.tono.stroppark.features.core.ui.setContentWithSnackbar
import fyi.tono.stroppark.features.map.domain.MapFilter
import fyi.tono.stroppark.features.map.ui.components.MapFilterChipRow
import fyi.tono.stroppark.features.map.ui.screens.MapScreen
import fyi.tono.stroppark.features.parking.domain.ParkingLocation
import fyi.tono.stroppark.features.parking.ui.ParkingViewModel
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class MapScreenTests: BaseUiTests() {
  private lateinit var viewModel: MapViewModel
  private lateinit var fakeParkingRepository: FakeParkingRepository
  private lateinit var fakeChargerRepository: FakeChargerRepository
  private lateinit var fakeLocationService: FakeLocationService
  private lateinit var fakePermissionService: FakeLocationPermissionService

  @BeforeTest
  fun setup() {
    fakeParkingRepository = FakeParkingRepository()
    fakeChargerRepository = FakeChargerRepository()
    fakeLocationService = FakeLocationService()
    fakePermissionService = FakeLocationPermissionService()
    viewModel = MapViewModel(
      parkingRepository = fakeParkingRepository,
      chargerRepository = fakeChargerRepository,
      locationService = fakeLocationService,
      locationPermission = fakePermissionService,
      debounceMs = 0L
    )
  }


  private val testSpots = listOf(
    ParkingLocation(
      id = "1",
      name = "Vrijdagmarkt",
      operator = "Stad Gent",
      availableCapacity = 150,
      totalCapacity = 400,
      open = true,
      free = false,
      lez = true,
      latitude = 51.0560,
      longitude = 3.7250
    ),
    ParkingLocation(
      id = "2",
      name = "Sint-Pietersplein",
      operator = "Stad Gent",
      availableCapacity = 0,
      totalCapacity = 700,
      open = true,
      free = true,
      lez = false,
      latitude = 51.0416,
      longitude = 3.7271
    )
  )

  private val testChargers = listOf(
    makeStationWithConnectors()
  )

  private fun makeStationWithConnectors() = StationWithConnectors(
    station = StationEntity(
      id = 1L,
      name = "Test Station",
      address = "Test Street 1",
      latitude = 51.05,
      longitude = 3.71,
      operator = null,
      usageCost = null,
      isOperational = true,
      numberOfPoints = 1,
      distanceKm = null,
    ),
    connectors = emptyList()
  )


  private fun makeStation(
    id: Long = 1L,
    name: String = "Station $id",
    isOperational: Boolean = true,
    hasFastCharge: Boolean = false,
    fastestChargerKw: Double? = null,
    usageCost: String? = null,
    latitude: Double? = null,
    longitude: Double? = null,
  ) = ChargerUiModel(
    id = id,
    name = name,
    address = "",
    operator = null,
    isOperational = isOperational,
    hasFastCharge = hasFastCharge,
    fastestChargerKw = fastestChargerKw,
    usageCost = usageCost,
    connectorSummary = "",
    numberOfPoints = null,
    distanceKm = null,
    latitude = latitude,
    longitude = longitude,
  )

  @Test
  fun `when permission is NotDetermined, RequestLocationPermission action is fired on launch`() = runComposeUiTest {
    fakePermissionService.state.value = LocationPermissionState.NotDetermined

    val testModule = module {
      factory { viewModel }
    }

    loadKoinModules(testModule)

    setContentWithSnackbar {
      MapScreen()
    }

    assertTrue(fakePermissionService.wasRequestCalled)
  }

  @Test
  fun `when permission is Granted, RequestLocationPermission action is NOT fired on launch`() = runComposeUiTest {
    fakePermissionService.state.value = LocationPermissionState.Granted
    fakeLocationService.shouldHaveLastKnownLocation = false

    val testModule = module {
      factory { viewModel }
    }

    loadKoinModules(testModule)

    setContentWithSnackbar {
      MapScreen(
        mapContent = { _, _, _ -> }
      )
    }

    assertFalse(fakePermissionService.wasRequestCalled, "Permission should NOT have been requested")
  }

  @Test
  fun `when parking filter clicked, only chargers markers remain`() = runComposeUiTest {
    fakePermissionService.state.value = LocationPermissionState.Granted
    val testModule = module {
      factory { viewModel }
    }

    fakeChargerRepository.dbFlow.emit(testChargers)
    fakeParkingRepository.dbFlow.emit(testSpots)

    loadKoinModules(testModule)

    setContentWithSnackbar {
      MapScreen(
        mapContent = { uiState, onAction, _ ->
          MapFilterChipRow(
            modifier = Modifier.padding(18.dp),
            availableFilters = MapFilter.entries.toSet(),
            activeFilters = uiState.activeFilters,
            onFilterToggle = { filter ->
              onAction(MapAction.ToggleFilter(filter))
            }
          )
        }
      )
    }

    onNodeWithTag(MapFilter.PARKING.name).performClick()

    assertFalse(viewModel.uiState.value.activeFilters.contains(MapFilter.PARKING), "parking should be disabled")
    assertTrue(viewModel.uiState.value.activeFilters.contains(MapFilter.CHARGERS), "charger should remain enabled")
  }

  @Test
  fun `when charger filter clicked, only parking markers remain`() = runComposeUiTest {
    fakePermissionService.state.value = LocationPermissionState.Granted
    val testModule = module {
      factory { viewModel }
    }

    fakeChargerRepository.dbFlow.emit(testChargers)
    fakeParkingRepository.dbFlow.emit(testSpots)

    loadKoinModules(testModule)

    setContentWithSnackbar {
      MapScreen(
        mapContent = { uiState, onAction, _ ->
          MapFilterChipRow(
            modifier = Modifier.padding(18.dp),
            availableFilters = MapFilter.entries.toSet(),
            activeFilters = uiState.activeFilters,
            onFilterToggle = { filter ->
              onAction(MapAction.ToggleFilter(filter))
            }
          )
        }
      )
    }

    onNodeWithTag(MapFilter.CHARGERS.name).performClick()

    assertFalse(viewModel.uiState.value.activeFilters.contains(MapFilter.CHARGERS), "charger should be disabled")
    assertTrue(viewModel.uiState.value.activeFilters.contains(MapFilter.PARKING), "parking should remain enabled")
  }

  @Test
  fun `by default all filters should be enabled`() = runComposeUiTest {
    fakePermissionService.state.value = LocationPermissionState.Granted
    val testModule = module {
      factory { viewModel }
    }

    fakeChargerRepository.dbFlow.emit(testChargers)
    fakeParkingRepository.dbFlow.emit(testSpots)

    loadKoinModules(testModule)

    setContentWithSnackbar {
      MapScreen(
        mapContent = { uiState, onAction, _ ->
          MapFilterChipRow(
            modifier = Modifier.padding(18.dp),
            availableFilters = MapFilter.entries.toSet(),
            activeFilters = uiState.activeFilters,
            onFilterToggle = { filter ->
              onAction(MapAction.ToggleFilter(filter))
            }
          )
        }
      )
    }


    assertTrue(viewModel.uiState.value.activeFilters.contains(MapFilter.PARKING), "parking should be enabled")
    assertTrue(viewModel.uiState.value.activeFilters.contains(MapFilter.CHARGERS), "charger should be enabled")
  }
}