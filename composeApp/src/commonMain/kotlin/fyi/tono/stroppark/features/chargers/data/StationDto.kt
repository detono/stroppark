package fyi.tono.stroppark.features.chargers.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StationDto(
  val id: Long,
  val name: String? = null,
  val address: String? = null,
  val latitude: Double,
  val longitude: Double,
  val operator: String? = null,
  @SerialName("usage_cost")
  val usageCost: String? = null,
  @SerialName("is_operational")
  val isOperational: Boolean? = null,
  @SerialName("number_of_points")
  val numberOfPoints: Int? = null,
  val distanceKm: Double? = null,
  val connectors: List<ConnectorDto>
)