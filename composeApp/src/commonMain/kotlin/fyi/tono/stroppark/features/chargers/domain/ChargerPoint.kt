package fyi.tono.stroppark.features.chargers.domain

data class ChargerPoint(
  val id: String,
  val address: String,
  val provider: String,
  val type: String,
  val powerKw: Int,
  val lat: Double,
  val lon: Double,
  val status: String
) {
  val isAvailable: Boolean = status.contains("In dienst", ignoreCase = true)
}