package fyi.tono.stroppark.features.chargers.data

import fyi.tono.stroppark.features.parking.data.ParkingDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChargingResponse(
  @SerialName("total_count") val totalCount: Int? = null,
  val results: List<ChargingPointDto>
)