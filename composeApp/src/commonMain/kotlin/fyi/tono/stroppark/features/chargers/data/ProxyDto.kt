package fyi.tono.stroppark.features.chargers.data

import kotlinx.serialization.Serializable

@Serializable
data class ProxyDto(
  val total: Int,
  val limit: Int,
  val offset: Int,
  val data: List<StationDto>
)
