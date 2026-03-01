package fyi.tono.stroppark.core.location

import fyi.tono.stroppark.core.network.dto.GhentCoordinatesDto
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreLocation.*
import platform.Foundation.*
import platform.darwin.NSObject
import kotlin.coroutines.resume

actual class LocationServiceImpl: LocationService {
  private val locationManager = CLLocationManager()

  @OptIn(ExperimentalForeignApi::class)
  actual override suspend fun getCurrentLocation(): GhentCoordinatesDto? = suspendCancellableCoroutine { continuation ->
    val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
      override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
        val location = didUpdateLocations.lastOrNull() as? CLLocation
        location?.let {
          val lat = it.coordinate.useContents { latitude }
          val lon = it.coordinate.useContents { longitude }

          if (continuation.isActive) {
            continuation.resume(GhentCoordinatesDto(lat, lon))
          }
        }
      }

      override fun locationManager(manager: CLLocationManager, didChangeAuthorizationStatus: CLAuthorizationStatus) {
        when (didChangeAuthorizationStatus) {
          kCLAuthorizationStatusAuthorizedWhenInUse,
          kCLAuthorizationStatusAuthorizedAlways -> {
            manager.requestLocation()
          }
          kCLAuthorizationStatusDenied,
          kCLAuthorizationStatusRestricted -> {
            if (continuation.isActive) continuation.resume(null)
          }
          else -> { /* Waiting for user choice */ }
        }
      }

      override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
        if (continuation.isActive) {
          continuation.resume(null)
        }
      }
    }

    locationManager.delegate = delegate

    continuation.invokeOnCancellation {
      locationManager.stopUpdatingLocation()
      locationManager.delegate = null
    }

    // Check if we already have permission or need to ask
    when (locationManager.authorizationStatus) {
      kCLAuthorizationStatusNotDetermined -> {
        locationManager.requestWhenInUseAuthorization()
      }
      kCLAuthorizationStatusAuthorizedWhenInUse,
      kCLAuthorizationStatusAuthorizedAlways -> {
        locationManager.requestLocation()
      }
      else -> {
        continuation.resume(null)
      }
    }
  }
}