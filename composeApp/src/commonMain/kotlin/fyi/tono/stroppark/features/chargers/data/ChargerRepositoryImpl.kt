package fyi.tono.stroppark.features.chargers.data

import co.touchlab.kermit.Logger
import fyi.tono.stroppark.BuildKonfig
import fyi.tono.stroppark.core.network.dto.GhentResponse
import fyi.tono.stroppark.features.chargers.database.ChargerDao
import fyi.tono.stroppark.features.chargers.database.ConnectorEntity
import fyi.tono.stroppark.features.chargers.database.StationEntity
import fyi.tono.stroppark.features.chargers.database.toEntity
import fyi.tono.stroppark.features.chargers.domain.ChargerPoint
import fyi.tono.stroppark.features.chargers.domain.ChargerRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock

class ChargerRepositoryImpl(
  private val logger: Logger = Logger.withTag("ParkingRepositoryImpl"),
  private val httpClient: HttpClient,
  private val dao: ChargerDao
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
      status = status,
    )
  }

  private val refreshMutex = Mutex()

  override suspend fun refreshStations(): Result<Unit> {
    if (refreshMutex.isLocked) return Result.success(Unit)

    return refreshMutex.withLock {
      runCatching {
        val limit = 1000
        var offset = 0
        val allEntities = mutableListOf<StationEntity>()
        val allConnectors = mutableListOf<ConnectorEntity>()

        val lastSynced = dao.getLastSyncedAt()
        val now = Clock.System.now().toString()

        do {
          val response: ProxyDto = httpClient.get("${BuildKonfig.API_BASE_URL}/stations") {
            header("X-API-KEY", BuildKonfig.API_KEY)
            parameter("limit", limit)
            parameter("offset", offset)
            lastSynced?.let { parameter("modified_since", it) }
          }.body()

          allEntities += response.data.map { it.toEntity() }
          allConnectors += response.data.flatMap { station ->
            station.connectors.map { it.toEntity(station.id) }
          }

          offset += response.data.size
        } while (offset < response.total)

        if (lastSynced == null) {
          dao.clearAndInsert(allEntities, allConnectors)
        } else {
          dao.insert(allEntities, allConnectors)
        }

        dao.setLastSyncedAt(now)
      }.onFailure { e ->
        logger.e("Failed to fetch chargers", e)
      }
    }
  }

  override fun getStationFlow() = dao.getStationsWithConnectors()
}