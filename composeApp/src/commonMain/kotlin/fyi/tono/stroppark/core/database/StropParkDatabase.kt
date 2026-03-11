package fyi.tono.stroppark.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import fyi.tono.stroppark.features.chargers.database.ChargerDao
import fyi.tono.stroppark.features.chargers.database.ConnectorEntity
import fyi.tono.stroppark.features.chargers.database.StationEntity
import fyi.tono.stroppark.features.parking.database.ParkingDao
import fyi.tono.stroppark.features.parking.database.ParkingEntity

@Database(
  entities = [
    ParkingEntity::class,
    StationEntity::class,
    ConnectorEntity::class
  ],
  version = 2
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class StropParkDatabase: RoomDatabase() {
  abstract val parkingDao: ParkingDao
  abstract val chargerDao: ChargerDao
}

// The Room compiler generates the `actual` implementations.
@Suppress(
  "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING",
  "KotlinNoActualForExpect"
)
expect object AppDatabaseConstructor : RoomDatabaseConstructor<StropParkDatabase> {
  override fun initialize(): StropParkDatabase
}