package fyi.tono.stroppark.features.chargers.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import fyi.tono.stroppark.features.chargers.ui.ChargerViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChargerListScreen(viewModel: ChargerViewModel = koinViewModel()) {
  // Assuming you've added FeatureViewModel.kt chargersState to your ViewModel
  val chargerState by viewModel.chargerState.collectAsState()

  Column(modifier = Modifier.fillMaxSize()) {
    TopAppBar(title = { Text("EV Charging Points") })

    LazyColumn(
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      content = {
        items(chargerState.chargers) { charger ->
          ChargerItem(charger)
        }
      }
    )
  }
}