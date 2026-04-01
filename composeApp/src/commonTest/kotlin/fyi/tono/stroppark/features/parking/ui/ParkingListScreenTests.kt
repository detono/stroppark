@file:OptIn(ExperimentalTestApi::class)

package fyi.tono.stroppark.features.parking.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.swipeDown
import fyi.tono.stroppark.core.location.LocationPermissionState
import fyi.tono.stroppark.fakes.FakeLocationPermissionService
import fyi.tono.stroppark.fakes.FakeLocationService
import fyi.tono.stroppark.fakes.FakeParkingRepository
import fyi.tono.stroppark.features.chargers.ui.screens.ChargerListScreen
import fyi.tono.stroppark.features.core.ui.BaseUiTests
import fyi.tono.stroppark.features.core.ui.setContentWithSnackbar
import fyi.tono.stroppark.features.parking.domain.ParkingFilter
import fyi.tono.stroppark.features.parking.domain.ParkingLocation
import fyi.tono.stroppark.features.parking.ui.screens.ParkingListScreen
import fyi.tono.stroppark.features.parking.ui.screens.ParkingListScreenContent
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class ParkingListScreenTest: BaseUiTests() {

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

  @Test
  fun `shows loading spinner when isLoading is true and list is empty`() = runComposeUiTest {
    setContent {
      ParkingListScreenContent(
        uiState = ParkingUiState(isLoading = true, parkingSpots = emptyList()),
        onAction = {}
      )
    }

    onNodeWithTag(ParkingTestTags.LOADING_SPINNER).assertIsDisplayed()
  }

  @Test
  fun `shows error message when error exists and list is empty`() = runComposeUiTest {
    val errorMsg = "Database locked."
    setContent {
      ParkingListScreenContent(
        uiState = ParkingUiState(isLoading = false, errorMessage = errorMsg, parkingSpots = emptyList()),
        onAction = {}
      )
    }

    onNodeWithText(errorMsg).assertIsDisplayed()
  }

  @Test
  fun `renders parking cards correctly with capacity labels`() = runComposeUiTest {
    setContentWithSnackbar {
      ParkingListScreenContent(
        uiState = ParkingUiState(isLoading = false, parkingSpots = testSpots),
        onAction = {}
      )
    }

    onNodeWithTag(ParkingTestTags.PARKING_LIST).assertIsDisplayed()
    onNodeWithTag(ParkingTestTags.CHIP_ROW).assertIsDisplayed()


    onNodeWithText("Vrijdagmarkt").assertIsDisplayed()
    onNodeWithText("Available").assertIsDisplayed()

    onNodeWithTag("1.${ParkingFilter.LEZ.name}", true).assertIsDisplayed()

    onNodeWithText("Sint-Pietersplein").assertIsDisplayed()
    onNodeWithText("Full").assertIsDisplayed()
    onNodeWithTag("2.${ParkingFilter.FREE.name}", true).assertIsDisplayed()
  }

  @Test
  fun `chips only shows free with free filter active`() = runComposeUiTest {
    val uiState = ParkingUiState(
      isLoading = false,
      parkingSpots = testSpots,
      activeFilters = setOf(ParkingFilter.FREE)
    )

    setContentWithSnackbar {
      ParkingListScreenContent(uiState = uiState, onAction = {})
    }

    // Sint-Pietersplein is free, so it should be visible
    onNodeWithText("Sint-Pietersplein").assertIsDisplayed()
    // Vrijdagmarkt is NOT free, so the UI should filter it out
    onNodeWithText("Vrijdagmarkt").assertDoesNotExist()
  }
  @Test
  fun `chip row only shows free if there are actually free`() = runComposeUiTest {
    val uiState = ParkingUiState(
      isLoading = false,
      parkingSpots = testSpots,
      activeFilters = setOf()
    )

    setContentWithSnackbar {

      ParkingListScreenContent(uiState = uiState, onAction = {})

    }

    onNodeWithTag(ParkingFilter.FREE.name).assertExists()
  }

  @Test
  fun `chip row doesnt show free if there are none free`() = runComposeUiTest {
    val uiState = ParkingUiState(
      isLoading = false,
      parkingSpots = testSpots.filterNot { it.free },
      activeFilters = setOf()
    )

    setContentWithSnackbar {
      ParkingListScreenContent(uiState = uiState, onAction = {})
    }

    onNodeWithTag(ParkingFilter.FREE.name).assertDoesNotExist()
  }

  @Test
  fun `clicking filter chip fires ToggleFilter action`() = runComposeUiTest {
    var capturedAction: ParkingAction? = null

    val uiState = ParkingUiState(
      isLoading = false,
      parkingSpots = testSpots,
      activeFilters = setOf(ParkingFilter.FREE)
    )

    setContentWithSnackbar {
      ParkingListScreenContent(uiState = uiState, onAction = { capturedAction = it })
    }

    onNodeWithTag(ParkingFilter.FREE.name).performClick()

    assertTrue(capturedAction is ParkingAction.ToggleFilter)
    assertEquals(ParkingFilter.FREE, (capturedAction as ParkingAction.ToggleFilter).filter)
  }

  @Test
  fun `clicking LEZ untoggles Avoid LEZ`() = runComposeUiTest {
    val capturedActions = mutableListOf<ParkingAction>()

    val uiState = ParkingUiState(
      isLoading = false,
      parkingSpots = testSpots,
      activeFilters = setOf(ParkingFilter.AVOID_LEZ)
    )

    setContentWithSnackbar {
      ParkingListScreenContent(uiState = uiState, onAction = { capturedActions.add(it) })
    }

    onNodeWithTag(ParkingFilter.LEZ.name).performClick()

    assertEquals(2, capturedActions.size)

    val firstAction = capturedActions[0] as ParkingAction.ToggleFilter
    val secondAction = capturedActions[1] as ParkingAction.ToggleFilter

    assertEquals(ParkingFilter.LEZ, firstAction.filter)
    assertEquals(ParkingFilter.AVOID_LEZ, secondAction.filter)
  }

  @Test
  fun `clicking parking card triggers navigation with correct URI`() = runComposeUiTest {
    var openedUri: String? = null

    val fakeUriHandler = object : UriHandler {
      override fun openUri(uri: String) { openedUri = uri }
    }

    setContentWithSnackbar {
      CompositionLocalProvider(LocalUriHandler provides fakeUriHandler) {
        ParkingListScreenContent(
          uiState = ParkingUiState(isLoading = false, parkingSpots = listOf(testSpots.first())),
          onAction = {}
        )
      }
    }

    onNodeWithText("Vrijdagmarkt").performClick()

    assertEquals(openedUri?.contains("51.056"), true)
    assertEquals(openedUri?.contains("3.725"), true)
  }

  @Test
  fun `swiping down triggers Refresh action`() = runComposeUiTest {
    var capturedAction: ParkingAction? = null

    val uiState = ParkingUiState(
      isLoading = false,
      parkingSpots = testSpots
    )

    setContentWithSnackbar {
      ParkingListScreenContent(
        uiState = uiState,
        onAction = { capturedAction = it }
      )
    }

    onNodeWithTag(ParkingTestTags.PARKING_LIST)
      .performTouchInput {
        swipeDown()
      }

    assertTrue(capturedAction is ParkingAction.Refresh)
  }

  @Test
  fun `renders filter chips but no cards when list is empty and not loading`() = runComposeUiTest {
    val uiState = ParkingUiState(
      isLoading = false,
      parkingSpots = emptyList(),
      errorMessage = null,
    )

    setContentWithSnackbar {
      ParkingListScreenContent(
        uiState = uiState,
        onAction = {}
      )
    }

    ParkingFilter.entries.forEach {
      onNodeWithTag(it.name).assertDoesNotExist()
    }
    onNodeWithText("Vrijdagmarkt").assertDoesNotExist()
  }

  @Test
  fun `shows rationale dialog when permission is Denied`() = runComposeUiTest {
    val fakeRepo = FakeParkingRepository()
    val fakeLocService = FakeLocationService()
    val fakePermService = FakeLocationPermissionService()

    // Force the permission state to Denied
    fakePermService.state.value = LocationPermissionState.Denied

    val testViewModel = ParkingViewModel(fakeRepo, fakeLocService, fakePermService)

    val testModule = module {
      factory { testViewModel }
    }
    //Overwrite with our module
    loadKoinModules(testModule)

    setContentWithSnackbar {
        ParkingListScreen()
    }

    onNodeWithText("Location needed").assertIsDisplayed()
    onNodeWithText("We need your location to calculate distances to nearby parking.").assertIsDisplayed()
  }

  @Test
  fun `when permission is NotDetermined, RequestLocationPermission action is fired on launch`() = runComposeUiTest {
    val fakeRepo = FakeParkingRepository()
    val fakeLocService = FakeLocationService()
    val fakePermService = FakeLocationPermissionService()


    fakePermService.state.value = LocationPermissionState.NotDetermined
    val testViewModel = ParkingViewModel(fakeRepo, fakeLocService, fakePermService)

    val testModule = module {
      factory { testViewModel }
    }

    loadKoinModules(testModule)

    setContentWithSnackbar {
      ParkingListScreen()
    }

    assertTrue(fakePermService.wasRequestCalled)
  }

  @Test
  fun `when permission is Granted, RequestLocationPermission action is NOT fired on launch`() = runComposeUiTest {
    val fakeRepo = FakeParkingRepository()
    val fakeLocService = FakeLocationService()
    val fakePermService = FakeLocationPermissionService()

    fakePermService.state.value = LocationPermissionState.Granted

    val testViewModel = ParkingViewModel(fakeRepo, fakeLocService, fakePermService)

    val testModule = module {
      factory { testViewModel }
    }

    loadKoinModules(testModule)

    setContentWithSnackbar {
      ParkingListScreen()
    }

    assertFalse(fakePermService.wasRequestCalled, "Permission should NOT have been requested")
  }
}