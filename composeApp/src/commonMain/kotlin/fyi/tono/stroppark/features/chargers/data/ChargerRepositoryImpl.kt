package fyi.tono.stroppark.features.chargers.data

import co.touchlab.kermit.Logger
import fyi.tono.stroppark.core.network.dto.GhentResponse
import fyi.tono.stroppark.features.chargers.domain.ChargerPoint
import fyi.tono.stroppark.features.chargers.domain.ChargerRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class ChargerRepositoryImpl(
  private val logger: Logger = Logger.withTag("ParkingRepositoryImpl"),
  private val httpClient: HttpClient,
): ChargerRepository {
  override suspend fun getChargers(): List<ChargerPoint> {
    val url = "https://data.stad.gent/api/explore/v2.1/catalog/datasets/laadpalen-gent/records"
    val results: MutableList<ChargerPoint> = mutableListOf()
    val limit = 100
    var offset = 0

    return try {
      do {
        val response = httpClient
          .get(url) {
            parameter("limit", limit)
            parameter("offset", offset)
          }
          .body<GhentResponse<ChargingPointDto>>()

        results.addAll(response.results.map { it.toDomain() })
        offset += limit

        val total = response.totalCount ?: break
      } while (results.size < total)

      results
    } catch (e: Exception) {
      logger.e("Failed to fetch chargers", e)
      emptyList()
    }
  }

  private fun ChargingPointDto.toDomain(): ChargerPoint {
    return ChargerPoint(
      id = urid,
      address = buildString {
        append(straatnaam)
        if (huisnummer?.isNotBlank() == true) {
          append(" ")
          append(huisnummer)
        }
      },
      provider = cpo,
      type = type,
      powerKw = kw,
      lat = geoPoint2d.lat,
      lon = geoPoint2d.lon,
      status = status
    )
  }
}