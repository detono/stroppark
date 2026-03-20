package fyi.tono.stroppark.fakes

import fyi.tono.stroppark.features.chargers.database.ChargerDao
import fyi.tono.stroppark.features.chargers.database.ConnectorEntity
import fyi.tono.stroppark.features.chargers.database.StationEntity
import fyi.tono.stroppark.features.chargers.database.StationWithConnectors
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlin.time.Instant

class FakeChargerDao: ChargerDao {
  private var storedConnectors: List<ConnectorEntity> = emptyList()
  private var storedStations: List<StationEntity> = emptyList()

  private val flowConnectors = MutableStateFlow<List<ConnectorEntity>>(emptyList())
  private val flowStations = MutableStateFlow<List<StationEntity>>(emptyList())

  private var lastSynced: String? = null

  override suspend fun insertStations(stations: List<StationEntity>) {
    storedStations = (storedStations + stations).distinctBy { it.id }
    flowStations.value = storedStations
  }

  override suspend fun clearAndInsert(
    stations: List<StationEntity>,
    connectors: List<ConnectorEntity>
  ) {
    // clear
    storedStations = emptyList()
    storedConnectors = emptyList()
    flowStations.value = emptyList()
    flowConnectors.value = emptyList()
    // insert connectors first so they're ready when flowStations emits
    storedConnectors = connectors
    flowConnectors.value = connectors
    storedStations = stations
    flowStations.value = stations
  }

  override suspend fun insertConnectors(connectors: List<ConnectorEntity>) {
    storedConnectors = (storedConnectors + connectors).distinctBy { it.stationId to it.typeName }
    flowConnectors.value = storedConnectors
  }

  override suspend fun insert(stations: List<StationEntity>, connectors: List<ConnectorEntity>) {
    insertStations(stations)
    insertConnectors(connectors)
  }

  override fun getStations(): Flow<List<StationEntity>> {
    return flowStations
  }

  override fun getConnectorsForStation(stationId: Long): Flow<List<ConnectorEntity>> {
    return flowConnectors.map { cons ->
      cons.filter { it.stationId == stationId }
    }
  }

  override suspend fun clearStations() {
    flowStations.value = emptyList()
    storedStations = emptyList()
  }

  override suspend fun clearConnectors() {
    flowConnectors.value = emptyList()
    storedConnectors = emptyList()
  }

  override fun getStationsWithConnectors(): Flow<List<StationWithConnectors>> {
    return flowStations.map { stations ->
      stations.map { stationEntity ->
        StationWithConnectors(
          station = stationEntity,
          connectors = storedConnectors.filter { it.stationId == stationEntity.id }
        )
      }
    }
  }

  override suspend fun getLastSyncedAt(): String? {
    return lastSynced
  }

  override suspend fun setLastSyncedAt(value: String) {
    lastSynced = value
  }

  fun getInsertedConnectors() = storedConnectors
  fun getInsertedStations() = storedStations
}