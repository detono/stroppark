package fyi.tono.stroppark.features.chargers.ui

import fyi.tono.stroppark.fakes.FakeChargerRepository
import fyi.tono.stroppark.fakes.FakeLocationPermissionService
import fyi.tono.stroppark.fakes.FakeLocationService
import fyi.tono.stroppark.features.chargers.domain.ChargerPoint
import fyi.tono.stroppark.features.core.ui.BaseViewModelTests
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue


class ChargerViewModelTests : BaseViewModelTests() {

  private val fakeLocationPermissionService = FakeLocationPermissionService()
  private val fakeLocationService = FakeLocationService()
  private val fakeRepo = FakeChargerRepository()

  @Test
  fun `initial state is loading with no data`() = runTest {
    val viewModel = ChargerViewModel(fakeRepo, fakeLocationService, fakeLocationPermissionService)
    val state = viewModel.uiState.value

    assertTrue(state.isLoading)
    assertTrue(state.chargers.isEmpty())
    assertNull(state.errorMessage)
  }

  @Test
  fun `fetchData updates uiState with success`() = runTest {
    val viewModel = ChargerViewModel(fakeRepo, fakeLocationService, fakeLocationPermissionService)
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
    assertEquals("Allego", state.chargers.first().operator)
  }

  @Test
  fun `failed fetch sets error message but keeps old data`() = runTest {
    val viewModel = ChargerViewModel(fakeRepo, fakeLocationService, fakeLocationPermissionService)

    // 1. Load some initial data successfully
    fakeRepo.mockData = listOf(
      ChargerPoint("1", "Address", "Provider", "AC", 22, 51.0, 3.7, "In dienst")
    )
    viewModel.fetchData()

    // 2. Trigger a failing refresh
    fakeRepo.shouldReturnError = true
    viewModel.fetchData(isSilent = true)

    val state = viewModel.uiState.value

    assertEquals(1, state.chargers.size)
    assertEquals("Could not update: Network Error", state.errorMessage)
  }
}