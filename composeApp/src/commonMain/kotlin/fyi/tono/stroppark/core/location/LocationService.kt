package fyi.tono.stroppark.core.location

import fyi.tono.stroppark.core.network.dto.GhentCoordinatesDto
import kotlinx.coroutines.flow.Flow

interface LocationService {
  suspend fun getLastKnownLocation(): GhentCoordinatesDto?
  suspend fun getCurrentLocation(): GhentCoordinatesDto?
  fun getLocationFlow(): Flow<GhentCoordinatesDto?>
}

