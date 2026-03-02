package fyi.tono.stroppark.features.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ParkingDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertParking(locations: List<ParkingEntity>)

  @Query("SELECT * FROM parking_locations")
  fun getParkingLocations(): Flow<List<ParkingEntity>>

  @Query("DELETE FROM parking_locations")
  suspend fun clearParking()

  @Transaction
  suspend fun clearAndInsert(locations: List<ParkingEntity>) {
    clearParking()
    insertParking(locations)
  }
}