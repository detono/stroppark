package fyi.tono.stroppark.features.chargers.data

import kotlinx.serialization.Serializable

@Serializable
data class GeometryWrapper(
  val type: String,
  val geometry: Geometry,
  val properties: Map<String, String> = emptyMap()
)