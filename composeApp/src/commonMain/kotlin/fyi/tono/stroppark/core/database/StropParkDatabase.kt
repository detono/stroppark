package fyi.tono.stroppark.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import fyi.tono.stroppark.features.chargers.database.ChargerDao
import fyi.tono.stroppark.features.chargers.database.ConnectorEntity
import fyi.tono.stroppark.features.chargers.database.StationEntity
import fyi.tono.stroppark.features.chargers.database.SyncMetadataEntity
import fyi.tono.stroppark.features.parking.database.ParkingDao
import fyi.tono.stroppark.features.parking.database.ParkingEntity

@Database(
  entities = [
    ParkingEntity::class,
    StationEntity::class,
    ConnectorEntity::class,
    SyncMetadataEntity::class
  ],
  version = 3
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class StropParkDatabase: RoomDatabase() {
  abstract val parkingDao: ParkingDao
  abstract val chargerDao: ChargerDao
}

// The Room compiler generates the `actual` implementations.
@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<StropParkDatabase> {
  override fun initialize(): StropParkDatabase
}