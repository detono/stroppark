package fyi.tono.stroppark.fakes

import fyi.tono.stroppark.features.chargers.database.ChargerDao
import fyi.tono.stroppark.features.chargers.database.ConnectorEntity
import fyi.tono.stroppark.features.chargers.database.StationEntity
import fyi.tono.stroppark.features.chargers.database.StationWithConnectors
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeChargerDao: ChargerDao {
  private var storedConnectors: List<ConnectorEntity> = emptyList()
  private var storedStations: List<StationEntity> = emptyList()

  private val flowConnectors = MutableStateFlow<List<ConnectorEntity>>(emptyList())
  private val flowStations = MutableStateFlow<List<StationEntity>>(emptyList())

  override suspend fun insertStations(stations: List<StationEntity>) {
    flowStations.value = stations
    storedStations = stations
  }

  override suspend fun insertConnectors(connectors: List<ConnectorEntity>) {
    flowConnectors.value = connectors
    storedConnectors = connectors
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

  fun getInsertedConnectors() = storedConnectors
  fun getInsertedStations() = storedStations
}