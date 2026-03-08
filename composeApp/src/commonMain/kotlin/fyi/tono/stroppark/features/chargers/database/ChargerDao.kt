package fyi.tono.stroppark.features.chargers.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ChargerDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertStations(stations: List<StationEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertConnectors(connectors: List<ConnectorEntity>)

  @Query("SELECT * FROM stations")
  fun getStations(): Flow<List<StationEntity>>

  @Query("SELECT * FROM connectors WHERE stationId = :stationId")
  fun getConnectorsForStation(stationId: Long): Flow<List<ConnectorEntity>>

  @Query("DELETE FROM stations")
  suspend fun clearStations()

  @Query("DELETE FROM connectors")
  suspend fun clearConnectors()

  @Transaction
  suspend fun clearAndInsert(stations: List<StationEntity>, connectors: List<ConnectorEntity>) {
    clearConnectors()
    clearStations()
    insertStations(stations)
    insertConnectors(connectors)
  }

  @Transaction
  @Query("SELECT * FROM stations")
  fun getStationsWithConnectors(): Flow<List<StationWithConnectors>>
}