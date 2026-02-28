package fyi.tono.stroppark.features.chargers.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import fyi.tono.stroppark.features.chargers.domain.ChargerPoint

@Composable
fun ChargerList(parkingSpots: List<ChargerPoint>) {
  LazyColumn(
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
    content = {
      items(parkingSpots) { parking ->
        ChargerItem(parking)
      }
    }
  )
}