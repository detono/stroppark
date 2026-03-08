package fyi.tono.stroppark.features.parking.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import fyi.tono.stroppark.features.parking.domain.ParkingFilter
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ParkingFilterChipRow(
  modifier: Modifier = Modifier,
  availableFilters: Set<ParkingFilter>,
  activeFilters: Set<ParkingFilter>,
  onFilterToggle: (ParkingFilter) -> Unit,
) {
  Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    availableFilters.forEach { filter ->
      val selected = filter in activeFilters
      FilterChip(
        modifier = Modifier.testTag(filter.name),
        selected = selected,
        onClick = { onFilterToggle(filter) },
        label = {
          Text(stringResource(filter.labelRes))
        },
        leadingIcon = if (selected) {
          { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
        } else null,
      )
    }
  }
}