package fyi.tono.stroppark.features.chargers.data

import kotlinx.serialization.Serializable

@Serializable
data class StationResponse(
  val status: String,
  val count: Int,
  val data: List<StationDto>
)