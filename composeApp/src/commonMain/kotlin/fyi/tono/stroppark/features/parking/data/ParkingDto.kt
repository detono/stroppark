package fyi.tono.stroppark.features.parking.data

import kotlinx.serialization.SerialName

@Serializable
data class ParkingDto(
  @SerialName("name") val name: String,
  @SerialName("totalcapacity") val total: Int,
  @SerialName("availablecapacity") val available: Int,
  @SerialName("description") val desc: String,
  @SerialName("location") val geo: List<Double> // [lon, lat]
)