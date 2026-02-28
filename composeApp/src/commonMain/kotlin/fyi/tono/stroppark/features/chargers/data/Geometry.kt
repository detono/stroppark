package fyi.tono.stroppark.features.chargers.data

import kotlinx.serialization.Serializable

@Serializable
data class Geometry(
  val type: String,
  val coordinates: List<Double>
)
