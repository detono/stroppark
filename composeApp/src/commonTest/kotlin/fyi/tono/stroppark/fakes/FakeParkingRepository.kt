package fyi.tono.stroppark.fakes

import fyi.tono.stroppark.features.parking.domain.ParkingLocation
import fyi.tono.stroppark.features.parking.domain.ParkingRepository
import kotlinx.coroutines.flow.MutableSharedFlow

class FakeParkingRepository : ParkingRepository {
  var shouldReturnError = false

  val dbFlow = MutableSharedFlow<List<ParkingLocation>>()

  override fun getParkingFlow() = dbFlow

  override suspend fun refreshParkingOccupancy(): Result<Unit> {
    return if (shouldReturnError) {
      Result.failure(Exception("Network Fail"))
    } else {
      Result.success(Unit)
    }
  }
}