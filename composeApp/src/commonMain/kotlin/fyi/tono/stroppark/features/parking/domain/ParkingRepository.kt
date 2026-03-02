package fyi.tono.stroppark.features.parking.domain

import kotlinx.coroutines.flow.Flow

interface ParkingRepository {
  suspend fun refreshParkingOccupancy(): Result<Unit>
  fun getParkingFlow(): Flow<List<ParkingLocation>>
}