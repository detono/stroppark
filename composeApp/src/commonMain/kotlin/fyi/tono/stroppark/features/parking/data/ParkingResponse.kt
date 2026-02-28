package fyi.tono.stroppark.features.parking.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ParkingResponse(
  @SerialName("total_count") val totalCount: Int? = null,
  val results: List<ParkingDto>
)