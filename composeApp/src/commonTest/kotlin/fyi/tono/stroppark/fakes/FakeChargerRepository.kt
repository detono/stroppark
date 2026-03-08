package fyi.tono.stroppark.fakes

import fyi.tono.stroppark.features.chargers.database.StationWithConnectors
import fyi.tono.stroppark.features.chargers.domain.ChargerPoint
import fyi.tono.stroppark.features.chargers.domain.ChargerRepository
import kotlinx.coroutines.flow.MutableSharedFlow

class FakeChargerRepository : ChargerRepository {
  var mockData = listOf<ChargerPoint>()
  var shouldReturnError = false

  val dbFlow = MutableSharedFlow<List<StationWithConnectors>>()

  override suspend fun getChargers(): List<ChargerPoint> {
    if (shouldReturnError) throw Exception("Network Error")
    return mockData
  }

  override suspend fun refreshStations(): Result<Unit> {
    return if (shouldReturnError) {
      Result.failure(Exception("Network Fail"))
    } else {
      Result.success(Unit)
    }
  }

  override fun getStationFlow() = dbFlow
}