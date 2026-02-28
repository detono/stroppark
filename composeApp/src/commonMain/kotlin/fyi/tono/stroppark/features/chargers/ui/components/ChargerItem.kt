package fyi.tono.stroppark.features.chargers.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EvStation
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import fyi.tono.stroppark.features.chargers.domain.ChargerPoint

@Composable
fun ChargerItem(charger: ChargerPoint) {
  ListItem(
    headlineContent = { Text(charger.address) },
    supportingContent = { Text("${charger.status} • ${charger.type}") },
    leadingContent = {
      Icon(
        imageVector = Icons.Default.EvStation,
        contentDescription = null,
        tint = if (charger.isAvailable) Color.Green else Color.Gray
      )
    },
    trailingContent = {
      Text("${charger.powerKw} kWh")
    }
  )
}