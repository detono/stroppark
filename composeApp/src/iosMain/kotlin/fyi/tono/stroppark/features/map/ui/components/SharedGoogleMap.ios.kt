package fyi.tono.stroppark.features.map.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import cocoapods.GoogleMaps.GMSCameraPosition
import cocoapods.GoogleMaps.GMSMapView
import platform.UIKit.CGRectZero

@Composable
actual fun SharedGoogleMap(
  modifier: Modifier
) {
  UIKitView(
    factory = {
      // Setting the initial camera position
      val camera = GMSCameraPosition.cameraWithLatitude(
        latitude = 50.9383,
        longitude = 4.0392,
        zoom = 12f
      )
      val mapView = GMSMapView.mapWithFrame(
        frame = CGRectZero.readValue(),
        camera = camera
      )
      mapView
    },
    modifier = modifier,
    interactive = true
  )
}