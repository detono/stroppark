package fyi.tono.stroppark.fakes

import fyi.tono.stroppark.core.location.LocationService
import fyi.tono.stroppark.core.network.dto.GhentCoordinatesDto

class FakeLocationService : LocationService {
  var mockLocation: GhentCoordinatesDto? = GhentCoordinatesDto(51.0543, 3.7174)

  override suspend fun getCurrentLocation(): GhentCoordinatesDto? = mockLocation
}