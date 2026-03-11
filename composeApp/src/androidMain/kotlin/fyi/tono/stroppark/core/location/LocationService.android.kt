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
import kotlinx.coroutines.tasks.await

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
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
  actual override fun getLocationUpdates(intervalMs: Long): Flow<GhentCoordinatesDto?> = callbackFlow {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
      .build()

    val callback = object : LocationCallback() {
      override fun onLocationResult(result: LocationResult) {
        result.lastLocation?.let { location ->
          trySend(GhentCoordinatesDto(location.latitude, location.longitude))
        }
      }
    }

    fusedLocationClient.requestLocationUpdates(
      locationRequest,
      callback,
      Looper.getMainLooper()
    ).addOnFailureListener { e ->
      close(e)
    }

    awaitClose {
      fusedLocationClient.removeLocationUpdates(callback)
    }
  }
}