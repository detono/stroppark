package fyi.tono.stroppark.core.location

import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

object LocationUtils {
  private const val EARTH_RADIUS_KM = 6371.0

  /**
   * Calculates distance in kilometres between two points
   */
  fun calculateDistance(
    lat1: Double, lon1: Double,
    lat2: Double, lon2: Double
  ): Double {
    val dLat = (lat2 - lat1).toRadians()
    val dLon = (lon2 - lon1).toRadians()

    val a = sin(dLat / 2).pow(2) +
        cos(lat1.toRadians()) * cos(lat2.toRadians()) *
        sin(dLon / 2).pow(2)

    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return EARTH_RADIUS_KM * c
  }

  private fun Double.toRadians(): Double = this * PI / 180.0

  fun formatDistance(km: Double): String {
    return if (km < 1.0) {
      "${(km * 1000).toInt()}m"
    } else {
      "test"
      //"${"%.1f".format(km)}km"
    }
  }
}