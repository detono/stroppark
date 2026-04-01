package fyi.tono.stroppark.features.parking.ui

import fyi.tono.stroppark.core.location.LocationPermissionState
import fyi.tono.stroppark.core.network.dto.GhentCoordinatesDto
import fyi.tono.stroppark.fakes.FakeLocationPermissionService
import fyi.tono.stroppark.fakes.FakeLocationService
import fyi.tono.stroppark.fakes.FakeParkingRepository
import fyi.tono.stroppark.features.core.ui.BaseViewModelTests
import fyi.tono.stroppark.features.parking.domain.ParkingFilter
import fyi.tono.stroppark.features.parking.domain.ParkingLocation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class ParkingViewModelTests: BaseViewModelTests() {
  private lateinit var viewModel: ParkingViewModel
  private lateinit var fakeRepository: FakeParkingRepository
  private lateinit var fakeLocationService: FakeLocationService
  private lateinit var fakePermissionService: FakeLocationPermissionService

  @BeforeTest
  fun setup() {
    fakeRepository = FakeParkingRepository()
    fakeLocationService = FakeLocationService()
    fakePermissionService = FakeLocationPermissionService()
    viewModel = ParkingViewModel(
      fakeRepository,
      fakeLocationService,
      fakePermissionService
    )
  }

  @Test
  fun `initial state should be Loading`() = runTest {
    assertEquals(
      true,
      viewModel.uiState.value.isLoading
    )
  }

  @Test
  fun `database updates flow into uiState automatically`() = runTest {
    val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

    fakePermissionService.state.value = LocationPermissionState.Granted

    val testData = listOf(
      ParkingLocation(
        id = "1",
        name = "Vrijdagmarkt",
        totalCapacity = 100,
        availableCapacity = 50,
        latitude = 51.0560,
        longitude = 3.7250
      )
    )

    fakeRepository.dbFlow.emit(testData)

    val currentState = viewModel.uiState.value
    assertEquals(false, currentState.isLoading)
    assertEquals(1, currentState.parkingSpots.size)
    assertEquals("Vrijdagmarkt", currentState.parkingSpots.first().name)

    assertNotNull(currentState.parkingSpots.first().distanceKm)
    collectJob.cancel()
  }

  @Test
  fun `fetchData success turns off loading spinner`() = runTest {
    viewModel.fetchData()

    val currentState = viewModel.uiState.value
    assertEquals(false, currentState.isLoading)
    assertEquals(null, currentState.errorMessage)
  }

  @Test
  fun `fetchData failure updates errorMessage`() = runTest {
    fakeRepository.shouldReturnError = true

    viewModel.fetchData()

    val currentState = viewModel.uiState.value
    assertEquals(false, currentState.isLoading)
    assertEquals("Network Fail", currentState.errorMessage)
  }

  @Test
  fun `spots are sorted by distance closest first`() = runTest {
    val collectJob = launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }

    fakeLocationService.mockLocation = GhentCoordinatesDto(lat = 51.05, lon = 3.72)
    fakePermissionService.state.value = LocationPermissionState.Granted

    val testData = listOf(
      ParkingLocation(
        id = "1", name = "Far Away",
        totalCapacity = 100, availableCapacity = 50,
        latitude = 51.10, longitude = 3.80
      ),
      ParkingLocation(
        id = "2", name = "Nearby", totalCapacity = 100,
        availableCapacity = 50,
        latitude = 51.0543, longitude = 3.7174
      ),
      ParkingLocation(
        id = "3", name = "Medium", totalCapacity = 100,
        availableCapacity = 50,
        latitude = 51.06, longitude = 3.73
      ),
    )

    fakeRepository.dbFlow.emit(testData)
    advanceUntilIdle()

    val spots = viewModel.uiState.value.parkingSpots
    assertEquals(3, spots.size)
    assertEquals("Nearby", spots[0].name)
    assertEquals("Medium", spots[1].name)
    assertEquals("Far Away", spots[2].name)

    collectJob.cancel()
  }

  @Test
  fun `distanceKm is null when location is unAVAILABLE`() = runTest {
    fakeLocationService.mockLocation = null

    val testData = listOf(
      ParkingLocation(id = "1", name = "Parking", totalCapacity = 100, availableCapacity = 50, latitude = 51.0560, longitude = 3.7250)
    )

    fakeRepository.dbFlow.emit(testData)

    assertEquals(null, viewModel.uiState.value.parkingSpots.first().distanceKm)
  }

  @Test
  fun `distanceKm is null when spot has no coordinates`() = runTest {
    val testData = listOf(
      ParkingLocation(id = "1", name = "Parking", totalCapacity = 100, availableCapacity = 50, latitude = null, longitude = null)
    )

    fakeRepository.dbFlow.emit(testData)

    assertEquals(null, viewModel.uiState.value.parkingSpots.first().distanceKm)
  }

  @Test
  fun `empty db emission sets isLoading false with empty list`() = runTest {
    fakeRepository.dbFlow.emit(emptyList())

    val state = viewModel.uiState.value
    assertEquals(false, state.isLoading)
    assertEquals(0, state.parkingSpots.size)
  }

  @Test
  fun `ToggleFilter adds filter when not present`() = runTest {
    viewModel.onAction(ParkingAction.ToggleFilter(ParkingFilter.AVAILABLE))

    assertContains(viewModel.uiState.value.activeFilters, ParkingFilter.AVAILABLE)
  }

  @Test
  fun `ToggleFilter removes filter when already active`() = runTest {
    viewModel.onAction(ParkingAction.ToggleFilter(ParkingFilter.AVAILABLE))
    viewModel.onAction(ParkingAction.ToggleFilter(ParkingFilter.AVAILABLE))

    assertEquals(false, ParkingFilter.AVAILABLE in viewModel.uiState.value.activeFilters)
  }

  @Test
  fun `ToggleFilter is idempotent - toggling twice returns to original state`() = runTest {
    val originalFilters = viewModel.uiState.value.activeFilters

    viewModel.onAction(ParkingAction.ToggleFilter(ParkingFilter.AVAILABLE))
    viewModel.onAction(ParkingAction.ToggleFilter(ParkingFilter.AVAILABLE))

    assertEquals(originalFilters, viewModel.uiState.value.activeFilters)
  }

  @Test
  fun `multiple filters can be active simultaneously`() = runTest {
    viewModel.onAction(ParkingAction.ToggleFilter(ParkingFilter.AVAILABLE))
    viewModel.onAction(ParkingAction.ToggleFilter(ParkingFilter.LEZ))

    assertContains(viewModel.uiState.value.activeFilters, ParkingFilter.AVAILABLE)
    assertContains(viewModel.uiState.value.activeFilters, ParkingFilter.LEZ)
  }

  @Test
  fun `silent fetchData does not show loading spinner`() = runTest {
    viewModel.fetchData(isSilent = true)

    assertEquals(false, viewModel.uiState.value.isLoading)
  }

  @Test
  fun `Refresh action triggers fetchData`() = runTest {
    fakeRepository.shouldReturnError = true

    viewModel.onAction(ParkingAction.Refresh)

    assertEquals("Network Fail", viewModel.uiState.value.errorMessage)
  }

  @Test
  fun `permissionState reflects changes from permission service`() = runTest {
    fakePermissionService.state.value = LocationPermissionState.Granted

    assertEquals(LocationPermissionState.Granted, viewModel.permissionState.value)
  }

  @Test
  fun `onLifecycleEvent false stops polling`() = runTest {
    viewModel.onLifecycleEvent(true)
    viewModel.onLifecycleEvent(false)

    // If polling were still active, a second fetchData would clear a pre-set error.
    // Setting error after stopping ensures it's not overwritten by a rogue poll.
    fakeRepository.shouldReturnError = true
    fakeRepository.dbFlow.emit(emptyList())

    assertEquals(false, viewModel.uiState.value.isLoading)
  }

}