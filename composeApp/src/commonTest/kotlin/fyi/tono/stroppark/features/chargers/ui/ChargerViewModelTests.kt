package fyi.tono.stroppark.features.chargers.ui

import fyi.tono.stroppark.fakes.FakeChargerRepository
import fyi.tono.stroppark.fakes.FakeLocationPermissionService
import fyi.tono.stroppark.fakes.FakeLocationService
import fyi.tono.stroppark.features.core.ui.BaseViewModelTests
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
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
    val viewModel = ChargerViewModel(fakeRepository, fakeLocationService, fakeLocationPermissionService)
    val state = viewModel.uiState.value

    assertTrue(state.isLoading)
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
  fun `fetchData failure updates errorMessage`() = runTest {
    fakeRepository.shouldReturnError = true

    viewModel.fetchData()

    advanceUntilIdle()

    val currentState = viewModel.uiState.value
    assertEquals(false, currentState.isLoading, "isLoading should be false")
    assertEquals("An unexpected error occurred", currentState.errorMessage, "errorMessage doesn't match")
  }
}