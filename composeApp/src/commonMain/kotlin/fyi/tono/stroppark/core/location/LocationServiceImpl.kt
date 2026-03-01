package fyi.tono.stroppark.core.location

import fyi.tono.stroppark.core.network.dto.GhentCoordinatesDto

expect class LocationServiceImpl: LocationService {
  override suspend fun getCurrentLocation(): GhentCoordinatesDto?
}