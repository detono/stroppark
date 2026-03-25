package fyi.tono.stroppark.features.chargers.data

import co.touchlab.kermit.Logger
import fyi.tono.stroppark.BuildKonfig
import fyi.tono.stroppark.core.network.dto.GhentResponse
import fyi.tono.stroppark.core.utils.CrashReporter
import fyi.tono.stroppark.core.utils.SyncProgress
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
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlin.time.Clock

class ChargerRepositoryImpl(
  private val logger: Logger = Logger.withTag("ParkingRepositoryImpl"),
  private val httpClient: HttpClient,
  private val dao: ChargerDao,
  private val crashReporter: CrashReporter
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
      crashReporter.recordException(e)
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

  override suspend fun refreshStations(): Flow<SyncProgress> = flow {
    if (!refreshMutex.tryLock()) return@flow

    try {
      val now = Clock.System.now().toString()
      val lastSynced = dao.getLastSyncedAt()
      val limit = 5_000


      val initialResponse: ProxyDto = httpClient.get("${BuildKonfig.API_BASE_URL}/stations") {
        header("X-API-KEY", BuildKonfig.API_KEY)
        parameter("limit", limit)
        parameter("offset", 0)
        lastSynced?.let { parameter("modified_since", it) }
      }.body()

      val total = initialResponse.total

      if (total == 0) {
        emit(SyncProgress(loaded = 0, total = total, done = true))
        refreshMutex.unlock()
        return@flow
      }

      val allEntities = mutableListOf<StationEntity>()
      val allConnectors = mutableListOf<ConnectorEntity>()

      var loaded = initialResponse.data.size
      emit(SyncProgress(loaded = loaded, total = total))

      val remainingOffsets = ((limit) until total step limit).toList()

      val concurrencySemaphore = Semaphore(5)
      coroutineScope {
        val deferredResponses = remainingOffsets.map { offset ->
          async {
            concurrencySemaphore.withPermit {
              val response: ProxyDto = httpClient.get("${BuildKonfig.API_BASE_URL}/stations") {
                header("X-API-KEY", BuildKonfig.API_KEY)
                parameter("limit", limit)
                parameter("offset", offset)
                lastSynced?.let { parameter("modified_since", it) }
              }.body()

              response.data
            }
          }
        }

        deferredResponses.forEach { deferred ->
          val data = deferred.await()

          allEntities += data.map { it.toEntity() }
          allConnectors += data.flatMap { station ->
            station.connectors.map { it.toEntity(station.id) }
          }

          loaded += data.size
          emit(SyncProgress(loaded = loaded, total = total))
        }
      }

      if (lastSynced == null) {
        dao.clearAndInsert(allEntities, allConnectors)
      } else {
        dao.insert(allEntities, allConnectors)
      }

      emit(SyncProgress(loaded = total, total = total, done = true))
      dao.setLastSyncedAt(now)
    } finally {
      refreshMutex.unlock()
    }
  }.catch { e ->
    logger.e("Failed to fetch chargers", e)
    crashReporter.recordException(e)
  }

  override fun getStationFlow() = dao.getStationsWithConnectors()
}