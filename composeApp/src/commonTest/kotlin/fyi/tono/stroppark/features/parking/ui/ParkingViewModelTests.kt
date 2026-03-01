package fyi.tono.stroppark.features.parking.ui

import fyi.tono.stroppark.core.location.LocationService
import fyi.tono.stroppark.core.location.LocationServiceImpl
import fyi.tono.stroppark.core.network.dto.GhentCoordinatesDto
import fyi.tono.stroppark.features.core.ui.BaseViewModelTests
import fyi.tono.stroppark.features.parking.domain.ParkingLocation
import fyi.tono.stroppark.features.parking.domain.ParkingRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals


class FakeParkingRepository : ParkingRepository {
  var shouldReturnError = false
  var mockData = listOf<ParkingLocation>()

  override suspend fun getParkingOccupancy(): List<ParkingLocation> {
    if (shouldReturnError) throw Exception("Network Fail")
    return mockData
  }
}
class FakeLocationService : LocationService {
  var mockLocation: GhentCoordinatesDto? = GhentCoordinatesDto(51.0543, 3.7174)

  override suspend fun getCurrentLocation(): GhentCoordinatesDto? = mockLocation
}

class ParkingViewModelTests: BaseViewModelTests() {
  private lateinit var viewModel: ParkingViewModel
  private lateinit var fakeRepository: FakeParkingRepository
  private lateinit var fakeLocationService: FakeLocationService

  @BeforeTest
  fun setup() {
    fakeRepository = FakeParkingRepository()
    fakeLocationService = FakeLocationService()
    viewModel = ParkingViewModel(fakeRepository, fakeLocationService)
  }

  @Test
  fun `initial state should be Loading`() = runTest {
    assertEquals(
      true,
      viewModel.uiState.value.isLoading
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
    assertEquals(false, currentState.isLoading)
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
    assertEquals(false, currentState.isLoading)
    assertContains( currentState.errorMessage ?: "", "Could not update")
  }
}