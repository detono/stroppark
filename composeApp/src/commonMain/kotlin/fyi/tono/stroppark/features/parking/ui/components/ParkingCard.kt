package fyi.tono.stroppark.features.parking.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fyi.tono.stroppark.core.ui.theme.GhentYellow
import fyi.tono.stroppark.features.parking.domain.ParkingLocation

@Composable
fun ParkingCard(parking: ParkingLocation) {
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
              text = parking.name,
              style = MaterialTheme.typography.titleMedium
            )

            val availabilityColor = when {
              parking.availableCapacity > 50 -> Color(0xFF4CAF50)
              parking.availableCapacity > 10 -> GhentYellow
              else -> MaterialTheme.colorScheme.error
            }

            Text(
              text = "${parking.availableCapacity} spaces available",
              color = availabilityColor,
              style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
            )
          }
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
              progress = { parking.availableCapacity.toFloat() / parking.totalCapacity },
              modifier = Modifier.size(40.dp),
              color = ProgressIndicatorDefaults.circularColor,
              strokeWidth = ProgressIndicatorDefaults.CircularStrokeWidth,
              trackColor = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
              strokeCap = ProgressIndicatorDefaults.CircularDeterminateStrokeCap,
            )
            Text(
              modifier = Modifier.padding(top = 8.dp),
              text = "${parking.availableCapacity}/${parking.totalCapacity}",
              style = MaterialTheme.typography.labelSmall
            )
          }
        }
      )
    }
  )
}