package fyi.tono.stroppark.core.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import fyi.tono.stroppark.features.database.ParkingDao
import fyi.tono.stroppark.features.database.ParkingEntity

@Database(entities = [ParkingEntity::class], version = 1)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class StropParkDatabase: RoomDatabase() {
  abstract val parkingDao: ParkingDao
}

// The Room compiler generates the `actual` implementations.
@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<StropParkDatabase> {
  override fun initialize(): StropParkDatabase
}