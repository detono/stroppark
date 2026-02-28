package fyi.tono.stroppark.features.chargers.data

import fyi.tono.stroppark.core.network.dto.GhentCoordinatesDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class ChargingPointDto(
  val geometry: GeometryWrapper,
  val objectid: Int,
  val status: String,
  val straatnaam: String? = null,
  val huisnummer: String? = null,
  val cpo: String,
  val fotolink: String? = null,
  val type: String,
  val kw: Int,
  val ligging: String,
  val url: String? = null,
  val urid: String,
  val synckey: String,
  val syncdate: String,
  val betrokkenadressen: String? = null,
  @SerialName("geo_point_2d")
  val geoPoint2d: GhentCoordinatesDto
)