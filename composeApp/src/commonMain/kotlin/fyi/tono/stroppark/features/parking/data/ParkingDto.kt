package fyi.tono.stroppark.features.parking.data

import fyi.tono.stroppark.core.network.dto.GhentCoordinatesDto
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class ParkingDto(
  val name: String,
  @SerialName("lastupdate")
  val lastUpdate: Instant,
  @SerialName("totalcapacity")
  val totalCapacity: Int,
  @SerialName("availablecapacity")
  val availableCapacity: Int,
  val occupation: Int,
  val type: String,
  val description: String,
  val id: String,
  @SerialName("openingtimesdescription")
  val openingTimesDescription: String,
  @SerialName("isopennow")
  val isOpenNow: Int,
  @SerialName("temporaryclosed")
  val temporaryClosed: Int,
  @SerialName("operatorinformation")
  val operatorInformation: String,
  @SerialName("freeparking")
  val freeParking: Int,
  @SerialName("urllinkaddress")
  val urlLinkAddress: String,
  @SerialName("occupancytrend")
  val occupancyTrend: String,
  @SerialName("locationanddimension")
  val locationAndDimension: String,
  val location: GhentCoordinatesDto? = null,
  val text: String? = null,
  @SerialName("categorie")
  val category: String,
  val dashboard: String,
)