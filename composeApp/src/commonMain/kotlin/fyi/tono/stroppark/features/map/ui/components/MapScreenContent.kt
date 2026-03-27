@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package fyi.tono.stroppark.features.map.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EvStation
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import eu.buney.maps.CameraPosition
import eu.buney.maps.CameraUpdateFactory
import eu.buney.maps.GoogleMap
import eu.buney.maps.LatLng
import eu.buney.maps.MapProperties
import eu.buney.maps.MapUiSettings
import eu.buney.maps.rememberCameraPositionState
import eu.buney.maps.utils.clustering.Clustering
import fyi.tono.stroppark.core.location.LocationPermissionState
import fyi.tono.stroppark.core.location.getGeoUri
import fyi.tono.stroppark.core.ui.theme.GhentCyan
import fyi.tono.stroppark.core.ui.theme.GhentGreen
import fyi.tono.stroppark.features.chargers.ui.components.ChargerItem
import fyi.tono.stroppark.features.map.domain.MapFilter
import fyi.tono.stroppark.features.map.domain.MapSelection
import fyi.tono.stroppark.features.map.domain.PoiType
import fyi.tono.stroppark.features.map.ui.MapAction
import fyi.tono.stroppark.features.map.ui.MapUiState
import fyi.tono.stroppark.features.parking.ui.components.ParkingCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreenContent(
  modifier: Modifier,
  uiState: MapUiState,
  onAction: (MapAction) -> Unit
) {
  val markers = remember(uiState.chargers, uiState.parking, uiState.activeFilters) {
    uiState.getMarkers()
  }

  val cameraPositionState = rememberCameraPositionState() {
    position = CameraPosition(
      target = LatLng(uiState.currentLocation.lat, uiState.currentLocation.lon),
      zoom = uiState.currentZoom
    )
  }

  var hasMovedToInitialLocation by remember { mutableStateOf(false) }

  LaunchedEffect(uiState.currentLocation) {
    if (!hasMovedToInitialLocation && uiState.currentLocation != MapUiState.DefaultLocation) {
      cameraPositionState.animate(
        CameraUpdateFactory.newLatLngZoom(
          LatLng(uiState.currentLocation.lat, uiState.currentLocation.lon),
          15f
        )
      )
      hasMovedToInitialLocation = true
    }
  }

  Box(
    modifier = Modifier.fillMaxSize()
  ) {
    AnimatedVisibility(
      modifier = Modifier.fillMaxSize(),
      visible = !uiState.isLoading,
      enter = EnterTransition.None,
      exit = fadeOut()
    ) {
      CircularProgressIndicator(
        modifier = Modifier
          .background(MaterialTheme.colorScheme.background)
          .wrapContentSize()
      )
    }

    when (uiState.mapSelection) {
      is MapSelection.Charger -> {
        val charger = uiState.mapSelection.charger

        MarkerModalBottomSheet(
          content = {
            if (charger.hasCoordinates) {
              NavigateButton(
                lat = charger.latitude!!,
                lng = charger.longitude!!,
                onClicked = { onAction(MapAction.DismissMarker) }
              )
            }

            ChargerItem(
              station = charger,
              onClick = { onAction(MapAction.DismissMarker) }
            )
          },
          onAction = onAction
        )
      }
      is MapSelection.Parking -> {
        val parking = uiState.mapSelection.location

        MarkerModalBottomSheet(
          content = {
            if (parking.hasCoordinates) {
              NavigateButton(
                lat = parking.latitude!!,
                lng = parking.longitude!!,
                onClicked = { onAction(MapAction.DismissMarker) }
              )
            }
            ParkingCard(
              parking = parking,
              onClick = { onAction(MapAction.DismissMarker) }
            )
          },
          onAction = onAction
        )

      }
      null -> {}
    }

    Column(
      modifier = Modifier.fillMaxSize(),
      content = {
        MapFilterChipRow(
          modifier = Modifier.padding(18.dp),
          availableFilters = MapFilter.entries.toSet(),
          activeFilters = uiState.activeFilters,
          onFilterToggle = { filter ->
            onAction(MapAction.ToggleFilter(filter))
          }
        )

        GoogleMap(
          cameraPositionState = cameraPositionState,
          uiSettings = MapUiSettings(zoomControlsEnabled = true),
          onMapLoaded = {
            onAction(MapAction.FinishedLoading)
          },
          modifier = modifier.fillMaxSize(),
          properties = MapProperties(isMyLocationEnabled = uiState.locationPermissionState == LocationPermissionState.Granted),
          content = {
            Clustering(
              items = markers,
              onClusterItemClick = { marker ->
                onAction(MapAction.SelectMarker(marker.id, marker.type))

                true
              },
              onClusterClick = { _ ->
                //No consume = default behavior
                false
              },
              clusterItemContent = { marker ->
                Box(
                  modifier = Modifier
                    .size(40.dp)
                    .background(
                      color = if (marker.type == PoiType.CHARGER) GhentGreen else GhentCyan,
                      shape = CircleShape
                    )
                    .padding(8.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = if (marker.type == PoiType.CHARGER) Icons.Default.EvStation else Icons.Default.LocalParking,
                    contentDescription = marker.title,
                    tint = Color.White
                  )
                }
              }
            )
          }
        )
      }
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarkerModalBottomSheet(
  content: @Composable () -> Unit,
  onAction: (MapAction) -> Unit
) {
  ModalBottomSheet(
    onDismissRequest = { onAction(MapAction.DismissMarker) },
    content = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        content = {
          content()
        }
      )
    }
  )
}

@Composable
fun NavigateButton(
  lat: Double,
  lng: Double,
  onClicked: () -> Unit
) {
  val uriHandler = LocalUriHandler.current

  TextButton(
    modifier = Modifier.fillMaxWidth(),
    onClick = {
      uriHandler.openUri(getGeoUri(lat, lng))
      onClicked()
    },
    content = {
      Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        text = "Navigate"
      )
    }
  )
}