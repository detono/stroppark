package fyi.tono.stroppark.features.parking.data

import co.touchlab.kermit.Logger
import fyi.tono.stroppark.core.network.dto.GhentResponse
import fyi.tono.stroppark.features.parking.database.ParkingDao
import fyi.tono.stroppark.features.parking.database.ParkingEntity
import fyi.tono.stroppark.features.parking.database.toDomain
import fyi.tono.stroppark.features.parking.database.toEntity
import fyi.tono.stroppark.features.parking.domain.ParkingLocation
import fyi.tono.stroppark.features.parking.domain.ParkingRepository
import fyi.tono.stroppark.features.parking.domain.ParkingType
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class ParkingRepositoryImpl(
  private val httpClient: HttpClient,
  private val json: Json,
  private val logger: Logger = Logger.withTag("ParkingRepositoryImpl"),
  private val dao: ParkingDao
) : ParkingRepository {
  override fun getParkingFlow(): Flow<List<ParkingLocation>> {
    return dao.getParkingLocations().map {
      logger.d("Emitting ${it.size} parking locations")
      it.map { it.toDomain() }
    }
  }

  override suspend fun refreshParkingOccupancy(): Result<Unit> {
    val url = "https://data.stad.gent/api/explore/v2.1/catalog/datasets/bezetting-parkeergarages-real-time/records?limit=100"

    return runCatching {
      val response = httpClient.get(url).body<GhentResponse<ParkingDto>>()
      val entities = response.results.map { it.toEntity() }

      dao.clearAndInsert(entities)
    }.onFailure { e ->
      logger.e("Failed to fetch parking locations", e)
    }
  }

  private fun ParkingDto.toEntity(): ParkingEntity {
    return toDomain(json).toEntity()
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