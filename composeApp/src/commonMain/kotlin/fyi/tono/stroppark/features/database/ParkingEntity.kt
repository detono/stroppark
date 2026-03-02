package fyi.tono.stroppark.features.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Clock

@Entity(tableName = "parking_locations")
data class ParkingEntity(
  @PrimaryKey val id: String,
  val name: String,
  val openingDescription: String,
  val operator: String,
  val availableCapacity: Int,
  val totalCapacity: Int,
  val lat: Double?,
  val lon: Double?,
  val type: String?,
  val open: Boolean?,
  val free: Boolean?,
  val lez: Boolean?,
  val lastUpdated: Long = Clock.System.now().toEpochMilliseconds()
)