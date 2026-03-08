package fyi.tono.stroppark.features.chargers.domain

import fyi.tono.stroppark.features.chargers.database.StationWithConnectors
import kotlinx.coroutines.flow.Flow

interface ChargerRepository {
  suspend fun getChargers(): List<ChargerPoint>

  suspend fun refreshStations(): Result<Unit>
  fun getStationFlow(): Flow<List<StationWithConnectors>>
}