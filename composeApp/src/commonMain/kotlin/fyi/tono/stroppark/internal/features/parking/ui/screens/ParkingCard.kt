package fyi.tono.stroppark.internal.features.parking.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode.Companion.Color
import androidx.compose.ui.unit.dp

@Composable
fun ParkingCard(parking: ParkingData) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    content = {
      Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = parking.name ?: "Unknown",
              style = MaterialTheme.typography.titleMedium
            )
            Text(
              text = "${parking.availableCapacity} spaces available",
              style = MaterialTheme.typography.bodyMedium,
              color = if ((parking.availableCapacity ?: 0) > 10) Color.Green else Color.Red
            )
          }
          // Occupancy Percentage Circle
          CircularProgressIndicator(
            progress = (parking.availableCapacity?.toFloat() ?: 0f) / (parking.totalCapacity ?: 1),
            modifier = Modifier.size(40.dp)
          )
        }
      )
    }
  )
}