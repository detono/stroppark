package fyi.tono.stroppark.features.parking.domain

import kotlin.time.Clock
import kotlin.time.Instant

data class ParkingLocation(
  val id: String = "",
  val name: String = "",
  val totalCapacity: Int = 0,
  val availableCapacity: Int = 0,
  val openingDescription: String = "",
  val url: String? = null,
  val operator: String = "",
  val latitude: Double? = null,
  val longitude: Double? = null,
  val phone: String? = null,
  val type: ParkingType? = null,
  val open: Boolean = false,
  val lez: Boolean = false,
  val free: Boolean = false,
  val lastUpdated: Instant = Clock.System.now(),
  val distanceKm: Double? = null
) {
  val occupancyProgress: Float = if (totalCapacity > 0)
    (totalCapacity - availableCapacity).toFloat() / totalCapacity
  else 0f
}