package fyi.tono.stroppark.features.parking.data

import co.touchlab.kermit.Logger
import fyi.tono.stroppark.core.network.dto.GhentResponse
import fyi.tono.stroppark.features.parking.domain.ParkingLocation
import fyi.tono.stroppark.features.parking.domain.ParkingRepository
import fyi.tono.stroppark.features.parking.domain.ParkingType
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.serialization.json.Json

class ParkingRepositoryImpl(
  private val httpClient: HttpClient,
  private val json: Json,
  private val logger: Logger = Logger.withTag("ParkingRepositoryImpl")
) : ParkingRepository {

  override suspend fun getParkingOccupancy(): List<ParkingLocation> {
    val url = "https://data.stad.gent/api/explore/v2.1/catalog/datasets/bezetting-parkeergarages-real-time/records?limit=100"

    return try {
      val response = httpClient.get(url).body<GhentResponse<ParkingDto>>()
      response.results.map { it.toDomain(json) }
    } catch (e: Exception) {
      logger.e("Failed to fetch parking locations", e)
      emptyList()
    }
  }

  private fun ParkingDto.toDomain(json: Json): ParkingLocation {
    val dimensions = locationAndDimension.let {
      try {
        json.decodeFromString<DimensionDto>(it)
      } catch (e: Exception) {
        logger.e("Failed to decode locationAndDimension", e)
        null
      }
    }

    return ParkingLocation(
      id = id,
      name = name,
      totalCapacity = totalCapacity,
      availableCapacity = availableCapacity,
      lez = !category.contains("buiten LEZ", ignoreCase = true),
      latitude = location?.lat,
      longitude = location?.lon,
      phone = dimensions?.contactDetailsTelephoneNumber,
      lastUpdated = lastUpdate,
      openingDescription = openingTimesDescription,
      url = urlLinkAddress,
      operator = operatorInformation,
      type = ParkingType.entries.find { it.type == type },
      open = isOpenNow == 1,
      free = freeParking == 1,
    )
  }
}