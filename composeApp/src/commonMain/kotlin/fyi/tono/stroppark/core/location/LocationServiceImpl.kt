package fyi.tono.stroppark.core.location

import fyi.tono.stroppark.core.network.dto.GhentCoordinatesDto
import kotlinx.coroutines.flow.Flow

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class LocationServiceImpl: LocationService {
  override suspend fun getLastKnownLocation(): GhentCoordinatesDto?
  override suspend fun getCurrentLocation(): GhentCoordinatesDto?
  override fun getLocationFlow(): Flow<GhentCoordinatesDto?>
}