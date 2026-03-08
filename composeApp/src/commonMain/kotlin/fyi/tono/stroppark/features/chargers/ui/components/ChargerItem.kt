package fyi.tono.stroppark.features.chargers.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.EuroSymbol
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import fyi.tono.stroppark.core.ui.components.molecules.InfoChip
import fyi.tono.stroppark.core.ui.components.molecules.StatusChip
import fyi.tono.stroppark.features.chargers.ui.ChargerUiModel

@Composable
fun ChargerItem(
  station: ChargerUiModel,
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null
) {
  val statusColor = when {
    !station.isOperational -> MaterialTheme.colorScheme.outline
    station.hasFastCharge  -> Color(0xFF4CAF50)
    else                   -> MaterialTheme.colorScheme.primary
  }
  val statusLabel = when {
    !station.isOperational -> "Offline"
    station.hasFastCharge  -> "Fast charge"
    else                   -> "Operational"
  }

  val animatedPower by animateFloatAsState(
    targetValue = if (station.isOperational) 1f else 0f,
    animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
    label = "power"
  )

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

      // ── Top row: name / operator + power badge ───────────────
      Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = station.name,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
          )
          station.operator?.let {
            Text(
              text = it,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }
        }

        Spacer(Modifier.width(12.dp))

        // Power ring — mirrors OccupancyRing style
        station.fastestChargerKw?.let { kw ->
          PowerBadge(
            powerKw = kw,
            animated = animatedPower,
            color = statusColor,
          )
        }
      }

      Spacer(Modifier.height(12.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
      Spacer(Modifier.height(12.dp))

      // ── Bottom row: chips + cost ─────────────────────────────
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        // Status chip
        StatusChip(
          label = statusLabel,
          dotColor = statusColor,
          containerColor = statusColor.copy(alpha = 0.12f),
          labelColor = statusColor,
        )

        // Fast charge chip
        if (station.hasFastCharge) {
          InfoChip(
            label = "DC Fast",
            icon = Icons.Rounded.Bolt,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
          )
        }

        Spacer(Modifier.weight(1f))

        // Usage cost
        station.usageCost?.takeIf { it.isNotBlank() }?.let { cost ->
          // Trim long cost strings
          val displayCost = if (cost.length > 12) cost.take(12) + "…" else cost
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Rounded.EuroSymbol,
              contentDescription = null,
              modifier = Modifier.size(14.dp),
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(4.dp))
            Text(
              text = displayCost,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
            )
          }
        }
      }

      // Connector summary line
      if (station.connectorSummary.isNotBlank()) {
        Spacer(Modifier.height(6.dp))
        Text(
          text = station.connectorSummary,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

// ── Power badge — mirrors OccupancyRing ─────────────────────────────────────

@Composable
private fun PowerBadge(
  powerKw: Double,
  animated: Float,
  color: Color,
) {
  Box(contentAlignment = Alignment.Center) {
    CircularProgressIndicator(
      progress = { 1f },
      modifier = Modifier.size(64.dp),
      strokeWidth = 6.dp,
      color = color.copy(alpha = 0.12f),
      strokeCap = StrokeCap.Round,
    )
    CircularProgressIndicator(
      progress = { animated },
      modifier = Modifier.size(64.dp),
      strokeWidth = 6.dp,
      color = color,
      strokeCap = StrokeCap.Round,
    )
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
        text = "${powerKw.toInt()}",
        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
        color = color,
      )
      Text(
        text = "kW",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}