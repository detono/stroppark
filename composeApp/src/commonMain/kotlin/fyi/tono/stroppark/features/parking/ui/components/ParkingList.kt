package fyi.tono.stroppark.features.parking.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import fyi.tono.stroppark.core.location.getGeoUri
import fyi.tono.stroppark.features.parking.domain.ParkingFilter
import fyi.tono.stroppark.features.parking.domain.ParkingLocation
import fyi.tono.stroppark.features.parking.ui.ParkingAction
import fyi.tono.stroppark.features.parking.ui.ParkingUiState


@Composable
fun ParkingList(
  uiState: ParkingUiState,
  onAction: (ParkingAction) -> Unit,
) {
  val activeFilters = uiState.activeFilters
  val parkingSpots = uiState.parkingSpots

  val uriHandler = LocalUriHandler.current

  val filtered = remember(parkingSpots, activeFilters) {
    if (activeFilters.isEmpty()) {
      parkingSpots
    } else {
      parkingSpots.filter { parking ->
        activeFilters.all { filter ->
          when (filter) {
            ParkingFilter.AVAILABLE -> parking.open && parking.availableCapacity > 0
            ParkingFilter.FREE      -> parking.free
            ParkingFilter.LEZ       -> parking.lez
            ParkingFilter.AVOID_LEZ -> !parking.lez
          }
        }
      }
    }
  }

  LazyColumn(
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
    content = {
      item {
        FilterChipRow(
          availableFilters = uiState.availableFilters,
          activeFilters = activeFilters,
          onFilterToggle = {
            onAction(ParkingAction.ToggleFilter(it))

            if (it == ParkingFilter.LEZ && activeFilters.contains(ParkingFilter.AVOID_LEZ)) {
              onAction(ParkingAction.ToggleFilter(ParkingFilter.AVOID_LEZ))
            }
            if (it == ParkingFilter.AVOID_LEZ && activeFilters.contains(ParkingFilter.LEZ)) {
              onAction(ParkingAction.ToggleFilter(ParkingFilter.LEZ))
            }
          }
        )
      }

      items(filtered) { parking ->
        ParkingCard(
          parking = parking,
          onClick = {
            val lat = parking.latitude
            val lng = parking.longitude

            if (lat != null && lng != null) {
              uriHandler.openUri(getGeoUri(lat, lng))
            }
          }
        )
      }
    }
  )
}