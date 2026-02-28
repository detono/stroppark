package fyi.tono.stroppark.features.chargers.data

import fyi.tono.stroppark.features.chargers.domain.ChargerPoint
import fyi.tono.stroppark.features.chargers.domain.ChargerRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class ChargerRepositoryImpl(
  private val httpClient: HttpClient,
): ChargerRepository {
  override suspend fun getChargers(): List<ChargerPoint> {
    val url = "https://data.stad.gent/api/explore/v2.1/catalog/datasets/laadpalen-gent/records?limit=100&offset=0"

    return try {
      val response = httpClient.get(url).body<ChargingResponse>()
      response.results.map { it.toDomain() }
    } catch (_: Exception) {
      emptyList()
    }
  }

  private fun ChargingPointDto.toDomain(): ChargerPoint {
    return ChargerPoint(
      id = urid,
      address = buildString {
        append(straatnaam)
        if (huisnummer.isNotBlank()) {
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