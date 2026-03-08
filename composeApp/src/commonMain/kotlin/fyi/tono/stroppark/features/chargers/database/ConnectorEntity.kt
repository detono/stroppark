package fyi.tono.stroppark.features.chargers.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "connectors",
  foreignKeys = [
    ForeignKey(
      entity = StationEntity::class,
      parentColumns = ["id"],
      childColumns = ["stationId"],
    )
  ],
  indices = [Index("stationId")]
)
data class ConnectorEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val stationId: Long,
  val typeName: String?,
  val formalName: String?,
  val powerKw: Double?,
  val amps: Double?,
  val voltage: Double?,
  val currentType: String?,
  val isFastCharge: Boolean?,
  val isOperational: Boolean?,
  val quantity: Int?
)