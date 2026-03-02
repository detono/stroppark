package fyi.tono.stroppark.features.parking.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.EnergySavingsLeaf
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fyi.tono.stroppark.core.location.getGeoUri
import fyi.tono.stroppark.core.ui.components.molecules.InfoChip
import fyi.tono.stroppark.core.ui.components.molecules.StatusChip
import fyi.tono.stroppark.core.ui.theme.GhentYellow
import fyi.tono.stroppark.core.utils.Platform
import fyi.tono.stroppark.core.utils.getPlatform
import fyi.tono.stroppark.features.parking.domain.ParkingLocation
import kotlin.math.roundToInt

@Composable
fun ParkingCard(
  parking: ParkingLocation,
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null
) {
  // ── Availability semantics ───────────────────────────────────────────────
  val availabilityColor = when {
    !parking.open                    -> MaterialTheme.colorScheme.outline
    parking.availableCapacity > 50   -> Color(0xFF4CAF50)
    parking.availableCapacity > 10   -> GhentYellow
    else                             -> MaterialTheme.colorScheme.error
  }
  val availabilityLabel = when {
    !parking.open                    -> "Closed"
    parking.availableCapacity == 0   -> "Full"
    parking.availableCapacity <= 10  -> "Almost full"
    parking.availableCapacity <= 50  -> "Limited"
    else                             -> "Available"
  }

  // Animate the ring when the card appears / data changes
  val animatedOccupancy by animateFloatAsState(
    targetValue = parking.occupancyProgress,
    animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
    label = "occupancy",
  )

  // ── Card shell ───────────────────────────────────────────────────────────
  Card(
    modifier = modifier.fillMaxWidth(),
    onClick = onClick ?: {},
    enabled = onClick != null,
    shape = MaterialTheme.shapes.large,
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    ),
    elevation = CardDefaults.cardElevation(
      defaultElevation = 2.dp,
      pressedElevation = 8.dp,
      hoveredElevation = 4.dp,
    ),
  ) {
    Column(modifier = Modifier.padding(16.dp)) {

      // ── Top row: name / operator + occupancy ring ────────────────
      Row(verticalAlignment = Alignment.CenterVertically) {

        // Left: name block
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = parking.name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
          if (parking.operator.isNotBlank()) {
            Text(
              text = parking.operator,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }

        Spacer(Modifier.width(12.dp))

        // Right: occupancy ring
        OccupancyRing(
          animatedOccupancy = animatedOccupancy,
          available = parking.availableCapacity,
          total = parking.totalCapacity,
          color = availabilityColor,
        )
      }

      Spacer(Modifier.height(12.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
      Spacer(Modifier.height(12.dp))

      // ── Bottom row: chips + distance ─────────────────────────────
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        // Availability status chip
        StatusChip(
          label = availabilityLabel,
          dotColor = availabilityColor,
          containerColor = availabilityColor.copy(alpha = 0.12f),
          labelColor = availabilityColor,
        )

        // Free parking chip
        if (parking.free) {
          InfoChip(
            label = "Free",
            icon = Icons.Rounded.LocalOffer,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
          )
        }

        // Low-emission zone chip
        if (parking.lez) {
          InfoChip(
            label = "LEZ",
            icon = Icons.Rounded.EnergySavingsLeaf,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
          )
        }

        Spacer(Modifier.weight(1f))

        if (parking.openingDescription.isNotBlank()) {
          Box(modifier = Modifier) {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Rounded.AccessTime,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              Spacer(Modifier.width(4.dp))
              Text(
                text = parking.openingDescription,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }

        // Distance from user
        parking.distanceKm?.let { km ->
          val whole = km.toInt()
          val decimal = ((km - whole) * 10).toInt()

          Text(
            text = if (km < 1.0) "${(km * 1000).roundToInt()} m"
            else "$whole.${decimal} km",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      /*Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        content = {
          // ── Opening hours ────────────────────────────────────────────


          /*if (parking.latitude != null && parking.longitude != null) {
            IconButton(
              onClick = {
                val lat = parking.latitude
                val lng = parking.longitude

                uriHandler.openUri(getGeoUri(lat, lng))
              },
              content = {
                Icon(
                  imageVector = Icons.Rounded.Navigation,
                  contentDescription = "Navigate",
                  modifier = Modifier.size(24.dp),
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,

                )
              }
            )
          }*/
        }
      )*/
    }
  }
}

// ── Occupancy ring ───────────────────────────────────────────────────────────

@Composable
private fun OccupancyRing(
  animatedOccupancy: Float,
  available: Int,
  total: Int,
  color: Color,
) {
  Box(contentAlignment = Alignment.Center) {
    // Faint full-circle track
    CircularProgressIndicator(
      progress = { 1f },
      modifier = Modifier.size(64.dp),
      strokeWidth = 6.dp,
      color = color.copy(alpha = 0.12f),
      strokeCap = StrokeCap.Round,
    )
    // Animated occupancy arc
    CircularProgressIndicator(
      progress = { animatedOccupancy },
      modifier = Modifier.size(64.dp),
      strokeWidth = 6.dp,
      color = color,
      strokeCap = StrokeCap.Round,
    )
    // Centre label
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
        text = "$available",
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        color = color,
      )
      Text(
        text = "/ $total",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}
