package fyi.tono.stroppark.features.chargers.database

import androidx.room.Embedded
import androidx.room.Relation

data class StationWithConnectors(
  @Embedded val station: StationEntity,
  @Relation(
    parentColumn = "id",
    entityColumn = "stationId"
  )
  val connectors: List<ConnectorEntity>
)