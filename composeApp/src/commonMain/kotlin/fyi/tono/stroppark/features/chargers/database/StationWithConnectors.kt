package fyi.tono.stroppark.features.chargers.database

import androidx.room.Embedded
import androidx.room.Relation
import fyi.tono.stroppark.features.map.domain.MapMarker
import fyi.tono.stroppark.features.map.domain.PoiType

data class StationWithConnectors(
  @Embedded val station: StationEntity,
  @Relation(
    parentColumn = "id",
    entityColumn = "stationId"
  )
  val connectors: List<ConnectorEntity>
) {
  fun toMarker(): MapMarker {
    return MapMarker(
      id = station.id.toString(),
      poiTitle = station.name ?: "",
      latitude = station.latitude,
      longitude = station.longitude,
      type = PoiType.CHARGER
    )
  }
}