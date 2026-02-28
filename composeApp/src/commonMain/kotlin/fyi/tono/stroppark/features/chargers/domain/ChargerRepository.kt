package fyi.tono.stroppark.features.chargers.domain

import fyi.tono.stroppark.features.chargers.data.ChargingPointDto

interface ChargerRepository {
  suspend fun getChargers(): List<ChargerPoint>
}