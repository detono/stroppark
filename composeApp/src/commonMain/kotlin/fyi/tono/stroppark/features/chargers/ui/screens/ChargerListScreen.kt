package fyi.tono.stroppark.features.chargers.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ChargerListScreen(viewModel: MainViewModel) {
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