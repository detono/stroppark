package fyi.tono.stroppark.fakes

import fyi.tono.stroppark.core.utils.SyncProgress
import fyi.tono.stroppark.features.chargers.database.StationWithConnectors
import fyi.tono.stroppark.features.chargers.domain.ChargerPoint
import fyi.tono.stroppark.features.chargers.domain.ChargerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

class FakeChargerRepository : ChargerRepository {
  var mockData = listOf<ChargerPoint>()
  var shouldEmitProgress = false
  var shouldReturnError = false

  val dbFlow = MutableStateFlow<List<StationWithConnectors>>(emptyList())

  override suspend fun getChargers(): List<ChargerPoint> {
    if (shouldReturnError) throw Exception("Network Error")
    return mockData
  }

  override suspend fun refreshStations(): Flow<SyncProgress> = flow {
      if (shouldReturnError) {
        error("Network Fail")
      }else if (shouldEmitProgress) {
        emit(SyncProgress(loaded = 50, total = 100, done = false))
      } else {
        emit(SyncProgress(loaded = 100, total = 100, done = true))
    }
  }

  override fun getStationFlow() = dbFlow
}