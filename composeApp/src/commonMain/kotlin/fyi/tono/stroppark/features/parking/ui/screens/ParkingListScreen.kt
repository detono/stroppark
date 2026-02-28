package fyi.tono.stroppark.features.parking.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fyi.tono.stroppark.features.parking.ui.ParkingUiState
import fyi.tono.stroppark.features.parking.ui.ParkingViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParkingListScreen(viewModel: ParkingViewModel = koinViewModel ()) {
  val uiState by viewModel.uiState.collectAsState()

  Column(modifier = Modifier.fillMaxSize()) {
    TopAppBar(title = { Text("Ghent Car Parks") })

    when (uiState) {
      ParkingUiState.Loading -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
      is ParkingUiState.Error -> {
        val message = (uiState as ParkingUiState.Error).message
        Text(message)
      }
      is ParkingUiState.Success -> {
        val spots = (uiState as ParkingUiState.Success).parkingSpots

        LazyColumn(
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
          content = {
            items(spots) { parking ->
              ParkingCard(parking)
            }
          }
        )
      }
    }
  }
}