package fyi.tono.stroppark.features.map.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.maps.android.compose.GoogleMap

@Composable
actual fun SharedGoogleMap(
  modifier: Modifier
) {
  // This is powered by the dependency you added!
  GoogleMap(
    modifier = modifier
  )
}