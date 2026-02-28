package fyi.tono.stroppark.features.parking.data

import kotlinx.serialization.Serializable

@Serializable
data class DimensionDto(
  val specificAccessInformation: List<String>,
  val level: String? = null,
  val roadNumber: String? = null,
  val roadName: String? = null,
  val contactDetailsTelephoneNumber: String? = null
)