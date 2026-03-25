package fyi.tono.stroppark.core.location

import fyi.tono.stroppark.core.network.dto.GhentCoordinatesDto
import kotlinx.coroutines.flow.Flow

expect class LocationServiceImpl: LocationService {
  override suspend fun getCurrentLocation(): GhentCoordinatesDto?
  override fun getLocationFlow(): Flow<GhentCoordinatesDto?>
}