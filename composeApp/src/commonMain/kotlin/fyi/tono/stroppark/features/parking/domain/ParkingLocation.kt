package fyi.tono.stroppark.features.parking.domain

import kotlin.time.Instant

data class ParkingLocation(
  val id: String,
  val name: String,
  val totalCapacity: Int,
  val availableCapacity: Int,
  val openingDescription: String,
  val url: String?,
  val operator: String,
  val latitude: Double?,
  val longitude: Double?,
  val phone: String?,
  val type: ParkingType?,
  val open: Boolean,
  val lez: Boolean,
  val free: Boolean,
  val lastUpdated: Instant
) {
  val occupancyProgress: Float = if (totalCapacity > 0)
    (totalCapacity - availableCapacity).toFloat() / totalCapacity
  else 0f
}