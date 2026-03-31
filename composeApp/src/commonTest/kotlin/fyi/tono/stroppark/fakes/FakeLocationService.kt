package fyi.tono.stroppark.fakes

import fyi.tono.stroppark.core.location.LocationService
import fyi.tono.stroppark.core.network.dto.GhentCoordinatesDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.yield

class FakeLocationService : LocationService {
  var mockLocation: GhentCoordinatesDto? = GhentCoordinatesDto(51.0543, 3.7174)

  var mockLocations = listOf(
    GhentCoordinatesDto(51.0543, 3.7174),
    GhentCoordinatesDto(51.0544, 3.7175),
    GhentCoordinatesDto(51.0545, 3.7176),
    GhentCoordinatesDto(51.0546, 3.7177),
    GhentCoordinatesDto(51.0547, 3.7178),
  )

  override suspend fun getLastKnownLocation(): GhentCoordinatesDto? {
    return mockLocation
  }

  override suspend fun getCurrentLocation(): GhentCoordinatesDto? = mockLocation
  override fun getLocationFlow(): Flow<GhentCoordinatesDto?> {
    return flow<GhentCoordinatesDto?> {
      while (true) {
        mockLocations.forEach {
          emit(it)

          delay(100)
          yield()
        }

        delay(500)
      }
    }.flowOn(Dispatchers.IO)
  }

}