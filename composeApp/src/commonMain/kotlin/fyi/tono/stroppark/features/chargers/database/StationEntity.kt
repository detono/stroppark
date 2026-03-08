package fyi.tono.stroppark.features.chargers.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Clock

@Entity(tableName = "stations")
data class StationEntity(
  @PrimaryKey val id: Long,
  val name: String?,
  val address: String?,
  val latitude: Double,
  val longitude: Double,
  val operator: String?,
  val usageCost: String?,
  val isOperational: Boolean?,
  val numberOfPoints: Int?,
  val distanceKm: Double? = null,
  val cachedAt: Long = Clock.System.now().toEpochMilliseconds(),
)


