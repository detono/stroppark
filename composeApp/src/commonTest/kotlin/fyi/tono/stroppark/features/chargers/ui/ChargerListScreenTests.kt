package fyi.tono.stroppark.features.chargers.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import fyi.tono.stroppark.fakes.FakeChargerRepository
import fyi.tono.stroppark.fakes.FakeLocationPermissionService
import fyi.tono.stroppark.fakes.FakeLocationService
import fyi.tono.stroppark.features.chargers.database.StationEntity
import fyi.tono.stroppark.features.chargers.database.StationWithConnectors
import fyi.tono.stroppark.features.chargers.domain.ChargerFilter
import fyi.tono.stroppark.features.chargers.ui.components.ChargerList
import fyi.tono.stroppark.features.chargers.ui.screens.ChargerListScreen
import fyi.tono.stroppark.features.core.ui.BaseUiTests
import fyi.tono.stroppark.features.core.ui.setContentWithSnackbar
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


@OptIn(ExperimentalTestApi::class)
class ChargerListScreenTests: BaseUiTests() {
  private lateinit var viewModel: ChargerViewModel
  private lateinit var fakeRepository: FakeChargerRepository
  private lateinit var fakeLocationService: FakeLocationService
  private lateinit var fakePermissionService: FakeLocationPermissionService

  @BeforeTest
  fun setup() {
    fakeRepository = FakeChargerRepository()
    fakeLocationService = FakeLocationService()
    fakePermissionService = FakeLocationPermissionService()
    viewModel = ChargerViewModel(
      fakeRepository,
      fakeLocationService,
      fakePermissionService
    )
  }

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

  private fun baseState(
    chargers: List<ChargerUiModel> = emptyList(),
    activeFilters: Set<ChargerFilter> = emptySet(),
  ) = ChargerUiState(
    isLoading = false,
    chargers = chargers,
    activeFilters = activeFilters,
  )

  @Test
  fun `chip row is shown`() = runComposeUiTest {
    setContentWithSnackbar {
      ChargerList(
        uiState = baseState(),
        onAction = {}
      )
    }

    onNodeWithTag(ChargerTestTags.CHIP_ROW).assertIsDisplayed()
  }

  @Test
  fun `all filter chips are shown`() = runComposeUiTest {
    setContentWithSnackbar {
      ChargerList(
        uiState = baseState(),
        onAction = {}
      )
    }

    ChargerFilter.entries.forEach { filter ->
      onNodeWithTag(filter.name).assertIsDisplayed()
    }
  }

  @Test
  fun `active filter chip appears selected`() = runComposeUiTest {
    setContentWithSnackbar {
      ChargerList(
        uiState = baseState(activeFilters = setOf(ChargerFilter.FAST_CHARGE)),
        onAction = {}
      )
    }

    onNodeWithTag(ChargerFilter.FAST_CHARGE.name).assertIsSelected()
    onNodeWithTag(ChargerFilter.FREE.name).assertIsNotSelected()
  }

  @Test
  fun `tapping a chip calls onAction with ToggleFilter`() = runComposeUiTest {
    val actions = mutableListOf<ChargerAction>()

    setContentWithSnackbar {
      ChargerList(
        uiState = baseState(),
        onAction = { actions += it }
      )
    }

    onNodeWithTag(ChargerFilter.FAST_CHARGE.name).performClick()

    assertEquals(1, actions.size)
    assertEquals(ChargerAction.ToggleFilter(ChargerFilter.FAST_CHARGE), actions.first())
  }

  @Test
  fun `enabling KW_150 also removes KW_22 and KW_50`() = runComposeUiTest {
    val actions = mutableListOf<ChargerAction>()

    setContentWithSnackbar {
      ChargerList(
        uiState = baseState(
          activeFilters = setOf(ChargerFilter.KW_22, ChargerFilter.KW_50)
        ),
        onAction = { actions += it }
      )
    }

    onNodeWithTag(ChargerFilter.KW_150.name).performClick()

    assertTrue(ChargerAction.ToggleFilter(ChargerFilter.KW_150) in actions)
    assertTrue(ChargerAction.ToggleFilter(ChargerFilter.KW_22) in actions)
    assertTrue(ChargerAction.ToggleFilter(ChargerFilter.KW_50) in actions)
  }

  @Test
  fun `enabling KW_50 also removes KW_22`() = runComposeUiTest {
    val actions = mutableListOf<ChargerAction>()

    setContentWithSnackbar {
      ChargerList(
        uiState = baseState(activeFilters = setOf(ChargerFilter.KW_22)),
        onAction = { actions += it }
      )
    }

    onNodeWithTag(ChargerFilter.KW_50.name).performClick()

    assertTrue(ChargerAction.ToggleFilter(ChargerFilter.KW_50) in actions)
    assertTrue(ChargerAction.ToggleFilter(ChargerFilter.KW_22) in actions)
  }

  @Test
  fun `fast charge filter only shows fast charge stations`() = runComposeUiTest {
    val chargers = listOf(
      makeStation(id = 1, name = "Fast One", hasFastCharge = true),
      makeStation(id = 2, name = "Slow One", hasFastCharge = false),
    )

    setContentWithSnackbar {
      ChargerList(
        uiState = baseState(
          chargers = chargers,
          activeFilters = setOf(ChargerFilter.FAST_CHARGE)
        ),
        onAction = {}
      )
    }

    onNodeWithText("Fast One").assertIsDisplayed()
    onNodeWithText("Slow One").assertDoesNotExist()
  }

  @Test
  fun `free filter only shows free stations`() = runComposeUiTest {
    val chargers = listOf(
      makeStation(id = 1, name = "Free One", usageCost = "Free"),
      makeStation(id = 2, name = "Paid One", usageCost = "€0.40/kWh"),
      makeStation(id = 3, name = "No Cost Info"),
    )

    setContentWithSnackbar {
      ChargerList(
        uiState = baseState(
          chargers = chargers,
          activeFilters = setOf(ChargerFilter.FREE)
        ),
        onAction = {}
      )
    }

    onNodeWithText("Free One").assertIsDisplayed()
    onNodeWithText("Paid One").assertDoesNotExist()
    onNodeWithText("No Cost Info").assertDoesNotExist()
  }

  @Test
  fun `kw22 filter only shows stations with at least 22kw`() = runComposeUiTest {
    val chargers = listOf(
      makeStation(id = 1, name = "11kW Station", fastestChargerKw = 11.0),
      makeStation(id = 2, name = "22kW Station", fastestChargerKw = 22.0),
      makeStation(id = 3, name = "50kW Station", fastestChargerKw = 50.0),
    )

    setContentWithSnackbar {
      ChargerList(
        uiState = baseState(
          chargers = chargers,
          activeFilters = setOf(ChargerFilter.KW_22)
        ),
        onAction = {}
      )
    }

    onNodeWithText("11kW Station").assertDoesNotExist()
    onNodeWithText("22kW Station").assertIsDisplayed()
    onNodeWithText("50kW Station").assertIsDisplayed()
  }

  @Test
  fun `no filter shows all stations`() = runComposeUiTest {
    val chargers = listOf(
      makeStation(id = 1, name = "Alpha"),
      makeStation(id = 2, name = "Beta"),
      makeStation(id = 3, name = "Gamma"),
    )

    setContentWithSnackbar {
      ChargerList(
        uiState = baseState(chargers = chargers),
        onAction = {}
      )
    }

    onNodeWithText("Alpha").assertIsDisplayed()
    onNodeWithText("Beta").assertIsDisplayed()
    onNodeWithText("Gamma").assertIsDisplayed()
  }

  @Test
  fun `multiple filters are combined with AND logic`() = runComposeUiTest {
    val chargers = listOf(
      makeStation(id = 1, name = "Fast and Free", hasFastCharge = true, usageCost = "Free"),
      makeStation(id = 2, name = "Fast but Paid", hasFastCharge = true, usageCost = "€0.40/kWh"),
      makeStation(id = 3, name = "Slow and Free", hasFastCharge = false, usageCost = "Free"),
    )

    setContentWithSnackbar {
      ChargerList(
        uiState = baseState(
          chargers = chargers,
          activeFilters = setOf(ChargerFilter.FAST_CHARGE, ChargerFilter.FREE)
        ),
        onAction = {}
      )
    }

    onNodeWithText("Fast and Free").assertIsDisplayed()
    onNodeWithText("Fast but Paid").assertDoesNotExist()
    onNodeWithText("Slow and Free").assertDoesNotExist()
  }

  @Test
  fun `charger list is shown`() = runComposeUiTest {
    setContentWithSnackbar {
      ChargerListScreen(
        viewModel = viewModel
      )
    }

    onNodeWithTag(ChargerTestTags.CHARGER_LIST).assertIsDisplayed()
  }

  @Test
  fun `charger list shown after data loads`() = runComposeUiTest {
    setContentWithSnackbar {
      ChargerListScreen(viewModel = viewModel)
    }

    fakeRepository.dbFlow.tryEmit(listOf(/* a StationWithConnectors */))

    onNodeWithTag(ChargerTestTags.CHARGER_LIST).assertIsDisplayed()
    onNodeWithTag(ChargerTestTags.LOADING_SPINNER).assertDoesNotExist()
  }

  @Test
  fun `error banner shown when error occurs with existing data`() = runComposeUiTest {
    fakeRepository.dbFlow.tryEmit(listOf(makeStationWithConnectors()))


    setContentWithSnackbar {
      ChargerListScreen(viewModel = viewModel)
    }

    awaitIdle()
    fakeRepository.shouldReturnError = true
    viewModel.fetchData().join()
    awaitIdle()

    onNodeWithTag(ChargerTestTags.ERROR_BANNER).assertIsDisplayed()
    onNodeWithTag(ChargerTestTags.CHARGER_LIST).assertIsDisplayed()
  }

  @Test
  fun `sync progress card shown during initial sync`() = runComposeUiTest {
    fakeRepository.shouldEmitProgress = true

    setContentWithSnackbar {
      ChargerListScreen(viewModel = viewModel)
    }

    viewModel.fetchData().join()
    awaitIdle()

    onNodeWithTag(ChargerTestTags.SYNC_PROGRESS_CARD).assertIsDisplayed()
  }
}