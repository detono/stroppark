package fyi.tono.stroppark.features.parking.data

import kotlinx.serialization.Serializable

@Serializable
data class DimensionDto(
  val specificAccessInformation: List<String>,
  val level: String?,
  val roadNumber: String?,
  val roadName: String?,
  val contactDetailsTelephoneNumber: String?
)