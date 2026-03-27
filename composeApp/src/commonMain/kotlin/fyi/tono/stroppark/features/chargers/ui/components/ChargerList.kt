package fyi.tono.stroppark.features.chargers.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import fyi.tono.stroppark.core.location.getGeoUri
import fyi.tono.stroppark.core.utils.LocalSnackbar
import fyi.tono.stroppark.features.chargers.domain.ChargerFilter
import fyi.tono.stroppark.features.chargers.ui.ChargerAction
import fyi.tono.stroppark.features.chargers.ui.ChargerTestTags
import fyi.tono.stroppark.features.chargers.ui.ChargerUiState
import fyi.tono.stroppark.features.parking.ui.ParkingTestTags
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import stroppark.composeapp.generated.resources.Res
import stroppark.composeapp.generated.resources.no_nav_app

@Composable
fun ChargerList(
  modifier: Modifier = Modifier,
  uiState: ChargerUiState,
  onAction: (ChargerAction) -> Unit,
) {
  val activeFilters = uiState.activeFilters
  val chargers = uiState.chargers

  val filtered = remember(chargers, activeFilters) {
    if (activeFilters.isEmpty()) {
      chargers
    } else {
      chargers.filter { charger ->
        activeFilters.all { filter ->
          when (filter) {
            ChargerFilter.FREE        -> charger.usageCost?.contains("free", ignoreCase = true) == true
            ChargerFilter.FAST_CHARGE -> charger.hasFastCharge
            ChargerFilter.KW_22       -> (charger.fastestChargerKw ?: 0.0) >= 22.0
            ChargerFilter.KW_50       -> (charger.fastestChargerKw ?: 0.0) >= 50.0
            ChargerFilter.KW_150      -> (charger.fastestChargerKw ?: 0.0) >= 150.0
          }
        }
      }
    }
  }

  val uriHandler = LocalUriHandler.current
  val snackBar = LocalSnackbar.current
  val scope = rememberCoroutineScope()

  LazyColumn(
    modifier = modifier,
    contentPadding = PaddingValues(
      start = 16.dp,
      end = 16.dp,
      top = 12.dp,
      bottom = 88.dp
    ),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    item {
      ChargerFilterChipRow(
        modifier = Modifier.testTag(ChargerTestTags.CHIP_ROW),
        availableFilters = uiState.availableFilters,
        activeFilters = activeFilters,
        onFilterToggle = {
          onAction(ChargerAction.ToggleFilter(it))

          if (it == ChargerFilter.KW_150) {
            if (activeFilters.contains(ChargerFilter.KW_22)) {
              onAction(ChargerAction.ToggleFilter(ChargerFilter.KW_22))
            }
            if (activeFilters.contains(ChargerFilter.KW_50)) {
              onAction(ChargerAction.ToggleFilter(ChargerFilter.KW_50))
            }
          }
          if (it == ChargerFilter.KW_50) {
            if (activeFilters.contains(ChargerFilter.KW_22)) {
              onAction(ChargerAction.ToggleFilter(ChargerFilter.KW_22))
            }
          }
        }
      )
    }


    items(filtered, key = { it.id }) { station ->
      ChargerItem(
        station = station,
        onClick = {
          val lat = station.latitude
          val lng = station.longitude

          if (lat != null && lng != null) {
            try {
              uriHandler.openUri(getGeoUri(lat, lng))
            } catch (_: IllegalArgumentException) {
              scope.launch {
                snackBar.showSnackbar(getString(Res.string.no_nav_app))
              }
            }
          }
        }
      )
    }
  }
}