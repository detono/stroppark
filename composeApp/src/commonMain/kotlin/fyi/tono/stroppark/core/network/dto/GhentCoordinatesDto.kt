package fyi.tono.stroppark.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class GhentCoordinatesDto(
  val lat: Double,
  val lon: Double
)