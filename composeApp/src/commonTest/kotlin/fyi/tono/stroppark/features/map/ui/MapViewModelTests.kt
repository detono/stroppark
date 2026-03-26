package fyi.tono.stroppark.features.map.ui

import fyi.tono.stroppark.fakes.FakeChargerRepository
import fyi.tono.stroppark.fakes.FakeLocationPermissionService
import fyi.tono.stroppark.fakes.FakeLocationService
import fyi.tono.stroppark.fakes.FakeParkingRepository
import fyi.tono.stroppark.features.core.ui.BaseViewModelTests
import fyi.tono.stroppark.features.map.domain.MapFilter
import fyi.tono.stroppark.features.map.domain.PoiType
import fyi.tono.stroppark.features.parking.domain.ParkingLocation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTests : BaseViewModelTests() {
  private val fakeChargerRepository = FakeChargerRepository()
  private val fakeParkingRepository = FakeParkingRepository()
  private val fakeLocationService = FakeLocationService()
  private val fakePermissionService = FakeLocationPermissionService()

  @Test
  fun `toggling a filter removes it if active and adds it if inactive`() = runTest {
    val viewModel = MapViewModel(
      fakeParkingRepository,
      fakeChargerRepository,
      fakeLocationService,
      fakePermissionService
    )

    val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
      viewModel.uiState.collect()
    }

    assertTrue(viewModel.uiState.value.activeFilters.contains(MapFilter.PARKING))

    viewModel.onAction(MapAction.ToggleFilter(MapFilter.PARKING))
    assertEquals(false, viewModel.uiState.value.activeFilters.contains(MapFilter.PARKING))

    viewModel.onAction(MapAction.ToggleFilter(MapFilter.PARKING))
    assertEquals(true, viewModel.uiState.value.activeFilters.contains(MapFilter.PARKING))

    collectJob.cancel()
  }

  @Test
  fun `selecting a marker updates mapSelection with correct item`() = runTest {
    val viewModel = MapViewModel(
      fakeParkingRepository,
      fakeChargerRepository,
      fakeLocationService,
      fakePermissionService
    )


    fakeParkingRepository.dbFlow.emit(listOf(ParkingLocation(
      id = "1"
    )))

    val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
      viewModel.uiState.collect()
    }


    viewModel.onAction(MapAction.SelectMarker(id = "1", type = PoiType.PARKING))

    val selection = viewModel.uiState.value.mapSelection
    assertNotNull(selection)

    collectJob.cancel()
  }

  @Test
  fun `dismissing marker clears mapSelection`() = runTest {
    val viewModel = MapViewModel(
      fakeParkingRepository,
      fakeChargerRepository,
      fakeLocationService,
      fakePermissionService
    )

    viewModel.onAction(MapAction.SelectMarker(id = "1", type = PoiType.CHARGER))

    viewModel.onAction(MapAction.DismissMarker)

    assertNull(viewModel.uiState.value.mapSelection)
  }
}