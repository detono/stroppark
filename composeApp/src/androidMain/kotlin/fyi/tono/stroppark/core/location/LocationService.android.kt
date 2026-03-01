package fyi.tono.stroppark.core.location

import android.annotation.SuppressLint
import android.content.Context
import co.touchlab.kermit.Logger
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import fyi.tono.stroppark.core.network.dto.GhentCoordinatesDto
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
}