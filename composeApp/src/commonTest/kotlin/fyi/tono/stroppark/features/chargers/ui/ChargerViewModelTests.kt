package fyi.tono.stroppark.features.chargers.ui

import fyi.tono.stroppark.features.chargers.domain.ChargerPoint
import fyi.tono.stroppark.features.chargers.domain.ChargerRepository
import fyi.tono.stroppark.features.core.ui.BaseViewModelTests
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FakeChargerRepository : ChargerRepository {
  var mockData = listOf<ChargerPoint>()
  var shouldFail = false

  override suspend fun getChargers(): List<ChargerPoint> {
    if (shouldFail) throw Exception("Network Error")
    return mockData
  }
}

class ChargerViewModelTests : BaseViewModelTests() {

  private val fakeRepo = FakeChargerRepository()

  @Test
  fun `initial state is loading with no data`() = runTest {
    val viewModel = ChargerViewModel(fakeRepo)
    val state = viewModel.uiState.value

    assertTrue(state.isLoading)
    assertTrue(state.chargers.isEmpty())
    assertNull(state.errorMessage)
  }

  @Test
  fun `fetchData updates uiState with success`() = runTest {
    val viewModel = ChargerViewModel(fakeRepo)
    val testPoint = ChargerPoint(
      "1",
      "Address",
      "Allego",
      "AC",
      22,
      51.0,
      3.7,
      "In dienst",
    )

    fakeRepo.mockData = listOf(testPoint)

    viewModel.fetchData()

    val state = viewModel.uiState.value
    assertEquals(1, state.chargers.size)
    assertEquals("Allego", state.chargers.first().provider)
  }

  @Test
  fun `failed fetch sets error message but keeps old data`() = runTest {
    val viewModel = ChargerViewModel(fakeRepo)

    // 1. Load some initial data successfully
    fakeRepo.mockData = listOf(
      ChargerPoint("1", "Address", "Provider", "AC", 22, 51.0, 3.7, "In dienst")
    )
    viewModel.fetchData()

    // 2. Trigger a failing refresh
    fakeRepo.shouldFail = true
    viewModel.fetchData(isSilent = true)

    val state = viewModel.uiState.value

    assertEquals(1, state.chargers.size)
    assertEquals("Could not update: Network Error", state.errorMessage)
  }
}