package fyi.tono.stroppark.features.chargers.ui

import fyi.tono.stroppark.core.location.LocationPermissionState
import fyi.tono.stroppark.core.network.dto.GhentCoordinatesDto
import fyi.tono.stroppark.fakes.FakeChargerRepository
import fyi.tono.stroppark.fakes.FakeLocationPermissionService
import fyi.tono.stroppark.fakes.FakeLocationService
import fyi.tono.stroppark.features.chargers.database.StationEntity
import fyi.tono.stroppark.features.chargers.database.StationWithConnectors
import fyi.tono.stroppark.features.core.ui.BaseViewModelTests
import io.ktor.http.HttpHeaders.Location
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ChargerViewModelTests : BaseViewModelTests() {
  private val fakeLocationPermissionService = FakeLocationPermissionService()
  private val fakeLocationService = FakeLocationService()
  private val fakeRepository = FakeChargerRepository()
  private val viewModel: ChargerViewModel = ChargerViewModel(
    fakeRepository,
    fakeLocationService,
    fakeLocationPermissionService
  )

  @Test
  fun `initial state is loading with no data`() = runTest {
    val standardDispatcher = StandardTestDispatcher(testScheduler)
    Dispatchers.setMain(standardDispatcher)

    val viewModel = ChargerViewModel(
      fakeRepository,
      fakeLocationService,
      fakeLocationPermissionService
    )

    val state = viewModel.uiState.value

    assertTrue(state.isLoading, "Should be loading initially")
    assertTrue(state.chargers.isEmpty())
    assertNull(state.errorMessage)
  }


  @Test
  fun `fetchData success turns off loading spinner`() = runTest {
    val viewModel = ChargerViewModel(
      fakeRepository,
      fakeLocationService,
      fakeLocationPermissionService
    )
    viewModel.fetchData()

    advanceUntilIdle()

    val currentState = viewModel.uiState.value
    assertEquals(false, currentState.isLoading)
    assertEquals(null, currentState.errorMessage)
  }

  @Test
  fun `fetchData failure updates errorMessage`() = runTest(testDispatcher) {
    fakeRepository.shouldReturnError = true

    viewModel.fetchData().join()

    advanceUntilIdle()

    val currentState = viewModel.uiState.value

    assertEquals(false, currentState.isLoading, "isLoading should be false")
    assertEquals("Could not update: Network Fail", currentState.errorMessage, "errorMessage doesn't match")
  }

  @Test
  fun `when permission is granted distances are calculated`() = runTest {
    val viewModel = ChargerViewModel(
      fakeRepository,
      fakeLocationService,
      fakeLocationPermissionService
    )

    val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
      viewModel.uiState.collect()
    }

    fakeLocationPermissionService.state.value = LocationPermissionState.Denied

    fakeRepository.dbFlow.emit(listOf(
      StationWithConnectors(
        StationEntity(
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
    ))

    fakeLocationPermissionService.state.value = LocationPermissionState.Granted

    advanceUntilIdle()

    val state = viewModel.uiState.value

    assertTrue(state.chargers.isNotEmpty(), "Chargers list should not be empty")
    assertNotNull(state.chargers.first().distanceKm)

    collectJob.cancel()
  }
}