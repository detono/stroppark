package fyi.tono.stroppark.features.parking.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import fyi.tono.stroppark.core.ui.theme.StropParkTheme
import fyi.tono.stroppark.features.parking.domain.ParkingLocation
import fyi.tono.stroppark.features.parking.ui.ParkingAction
import fyi.tono.stroppark.features.parking.ui.ParkingTestTags
import fyi.tono.stroppark.features.parking.ui.ParkingUiState
import fyi.tono.stroppark.features.parking.ui.components.ParkingList


@Composable
fun ParkingListScreenContent(uiState: ParkingUiState, onAction: (ParkingAction) -> Unit) {

  PullToRefreshBox(
    modifier = Modifier.fillMaxSize(),
    isRefreshing = uiState.isLoading,
    onRefresh = {
      onAction(ParkingAction.Refresh)
    },
    content = {
      when {
        uiState.isLoading && uiState.parkingSpots.isEmpty() -> {
          CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center)
              .testTag(ParkingTestTags.LOADING_SPINNER)
          )
        }

        uiState.errorMessage != null && uiState.parkingSpots.isEmpty() -> {
          Text(
            text = uiState.errorMessage,
            modifier = Modifier.align(Alignment.Center).testTag(ParkingTestTags.ERROR_BANNER)
          )
        }

        else -> {
          ParkingList(
            uiState = uiState,
            onAction = onAction
          )
        }
      }

      if (uiState.errorMessage != null && uiState.parkingSpots.isNotEmpty()) {
        Surface(
          color = MaterialTheme.colorScheme.errorContainer,
          modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
        ) {
          Text(
            text = uiState.errorMessage,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(8.dp),
            textAlign = TextAlign.Center
          )
        }
      }
    }
  )
}

@Preview
@Composable
fun ParkingListScreenContentWithoutLocationPreview() {
  // Wrap it in your theme so the colours are correct!
  StropParkTheme {
    ParkingListScreenContent(
      uiState = ParkingUiState(
        isLoading = false,
        parkingSpots = listOf(
          ParkingLocation(
            id = "1",
            name = "Vrijdagmarkt",
            availableCapacity = 150,
            totalCapacity = 400,
          ),
          ParkingLocation(
            id = "2",
            name = "Reep",
            availableCapacity = 0,
            totalCapacity = 250,
          )
        )
      ),
      onAction = {}
    )
  }
}

@Preview
@Composable
fun ParkingListScreenContentPreview() {
  // Wrap it in your theme so the colours are correct!
  StropParkTheme {
    ParkingListScreenContent(
      uiState = ParkingUiState(
        isLoading = false,
        parkingSpots = listOf(
          ParkingLocation(
            id = "1",
            name = "Vrijdagmarkt",
            availableCapacity = 150,
            totalCapacity = 400,
            distanceKm = 1.2
          ),
          ParkingLocation(
            id = "2",
            name = "Reep",
            availableCapacity = 0,
            totalCapacity = 250,
            distanceKm = 0.5
          )
        )
      ),
      onAction = {}
    )
  }
}

@Preview(showSystemUi = true)
@Composable
fun ParkingListScreenContentLoadingPreview() {
  StropParkTheme {
    ParkingListScreenContent(
      uiState = ParkingUiState(isLoading = true, parkingSpots = emptyList()),
      onAction = {}
    )
  }
}