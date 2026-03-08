package fyi.tono.stroppark.fakes

import fyi.tono.stroppark.features.parking.database.ParkingDao
import fyi.tono.stroppark.features.parking.database.ParkingEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeParkingDao: ParkingDao {
  private var stored: List<ParkingEntity> = emptyList()
  private val flow = MutableStateFlow<List<ParkingEntity>>(emptyList())

  override suspend fun insertParking(locations: List<ParkingEntity>) {
    stored = locations
    flow.value = locations
  }

  override fun getParkingLocations(): Flow<List<ParkingEntity>> = flow

  override suspend fun clearParking() {
    stored = emptyList()
    flow.value = emptyList()
  }

  fun getInsertedLocations(): List<ParkingEntity> = stored
}