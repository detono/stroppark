package fyi.tono.stroppark.features.parking.ui

import fyi.tono.stroppark.BaseViewModelTest
import fyi.tono.stroppark.features.parking.domain.ParkingLocation
import fyi.tono.stroppark.features.parking.domain.ParkingRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class FakeParkingRepository : ParkingRepository {
  var shouldReturnError = false
  var mockData = listOf<ParkingLocation>()

  override suspend fun getParkingOccupancy(): List<ParkingLocation> {
    if (shouldReturnError) throw Exception("Network Fail")
    return mockData
  }
}

class ParkingViewModelTests: BaseViewModelTest() {
  private lateinit var viewModel: ParkingViewModel
  private lateinit var fakeRepository: FakeParkingRepository

  @BeforeTest
  fun setup() {
    fakeRepository = FakeParkingRepository()
    viewModel = ParkingViewModel(fakeRepository)
  }

  @Test
  fun `initial state should be Loading`() = runTest {
    assertEquals(
      ParkingUiState.Loading,
      viewModel.uiState.value
    )
  }

  @Test
  fun `successful load updates uiState to Success`() = runTest {
    // 1. Prepare data
    val testData = listOf(
      ParkingLocation(
        id = "1",
        name = "Vrijdagmarkt",
        totalCapacity = 100,
        availableCapacity = 50
      )
    )

    fakeRepository.mockData = testData

    // 2. Trigger load
    viewModel.fetchData()

    // 3. Assert
    val currentState = viewModel.uiState.value
    assertTrue(currentState is ParkingUiState.Success)
    assertEquals(1, currentState.parkingSpots.size)
    assertEquals("Vrijdagmarkt", currentState.parkingSpots.first().name)
  }

  @Test
  fun `failed load updates uiState to Error`() = runTest {
    // 1. Force an error
    fakeRepository.shouldReturnError = true

    // 2. Act
    viewModel.fetchData()

    // 3. Assert
    val currentState = viewModel.uiState.value
    assertTrue(currentState is ParkingUiState.Error)
    assertEquals("Network Fail", currentState.message)
  }
}