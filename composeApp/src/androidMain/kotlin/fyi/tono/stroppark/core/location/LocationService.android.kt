package fyi.tono.stroppark.core.location

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import co.touchlab.kermit.Logger
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import fyi.tono.stroppark.core.network.dto.GhentCoordinatesDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.tasks.await

actual class LocationServiceImpl(
  private val context: Context,
  private val logger: Logger = Logger.withTag("LocationService")
) : LocationService {
  @SuppressLint("MissingPermission")
  actual override suspend fun getCurrentLocation(): GhentCoordinatesDto? {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    return try {
      val location = fusedLocationClient.getCurrentLocation(
        Priority.PRIORITY_BALANCED_POWER_ACCURACY,
        null
      ).await()

      location?.let { GhentCoordinatesDto(it.latitude, it.longitude) }
    } catch (e: Exception) {
      logger.e("Error getting location", e)
      null
    }
  }

  @SuppressLint("MissingPermission")
  actual override fun getLocationFlow(): Flow<GhentCoordinatesDto?> = callbackFlow {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    val locationRequest = LocationRequest.Builder(
      Priority.PRIORITY_BALANCED_POWER_ACCURACY,
      10_000L
    ).apply {
      setMinUpdateDistanceMeters(100f)
    }.build()

    val callback = object : LocationCallback() {
      override fun onLocationResult(result: LocationResult) {
        result.lastLocation?.let { loc ->
          trySend(GhentCoordinatesDto(loc.latitude, loc.longitude))
        }
      }
    }

    try {
      fusedLocationClient.requestLocationUpdates(
        locationRequest,
        callback,
        Looper.getMainLooper()
      )
      logger.i("Started location flow updates")
    } catch (e: Exception) {
      logger.e("Failed to start location updates", e)
      close(e)
    }

    awaitClose {
      logger.i("Stopping location flow updates to save battery")
      fusedLocationClient.removeLocationUpdates(callback)
    }
  }.conflate()
}