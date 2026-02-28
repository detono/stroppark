package fyi.tono.stroppark.features.parking.data

import kotlinx.serialization.Serializable

@Serializable
data class LatLonDto(
  val lat: Double,
  val lon: Double
)