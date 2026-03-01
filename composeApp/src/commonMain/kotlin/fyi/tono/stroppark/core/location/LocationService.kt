package fyi.tono.stroppark.core.location

import fyi.tono.stroppark.core.network.dto.GhentCoordinatesDto

interface LocationService {
  suspend fun getCurrentLocation(): GhentCoordinatesDto?
}