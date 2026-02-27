package fyi.tono.stroppark.features.parking.domain

data class ParkingLocation(
  val id: String,
  val name: String,
  val description: String,
  val totalCapacity: Int,
  val availableCapacity: Int,
  val isLez: Boolean,
  val lat: Double,
  val lon: Double
) {
  val occupancyPercentage: Float = if (totalCapacity > 0)
    (totalCapacity - availableCapacity).toFloat() / totalCapacity
  else 0f
}