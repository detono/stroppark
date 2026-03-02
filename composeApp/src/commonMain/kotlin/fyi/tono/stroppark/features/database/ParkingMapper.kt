package fyi.tono.stroppark.features.database

import fyi.tono.stroppark.features.parking.data.ParkingDto
import fyi.tono.stroppark.features.parking.domain.ParkingLocation
import fyi.tono.stroppark.features.parking.domain.ParkingType
import kotlin.time.Instant

fun ParkingEntity.toDomain(): ParkingLocation {
  return ParkingLocation(
    id = id,
    name = name,
    totalCapacity = totalCapacity,
    availableCapacity = availableCapacity,
    openingDescription = openingDescription,
    url = null,
    operator = operator,
    latitude = lat,
    longitude = lon,
    phone = null,
    type = ParkingType.entries.find { it.type == type },
    open = open == true,
    lez = lez == true,
    free = free == true,
    lastUpdated = Instant.fromEpochMilliseconds(lastUpdated),
    distanceKm = null
  )
}

fun ParkingLocation.toEntity(): ParkingEntity {
  return ParkingEntity(
    id = id,
    name = name,
    openingDescription = openingDescription,
    operator = operator,
    availableCapacity = availableCapacity,
    totalCapacity = totalCapacity,
    lat = latitude,
    lon = longitude,
    type = type?.type,
    free = free,
    open = open,
    lez = lez,
    lastUpdated = lastUpdated.toEpochMilliseconds()
  )
}