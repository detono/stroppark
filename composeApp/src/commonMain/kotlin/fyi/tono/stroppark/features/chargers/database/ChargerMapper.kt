package fyi.tono.stroppark.features.chargers.database

import fyi.tono.stroppark.features.chargers.data.ConnectorDto
import fyi.tono.stroppark.features.chargers.data.StationDto

fun StationDto.toEntity() = StationEntity(
  id = id,
  name = name,
  address = address,
  latitude = latitude,
  longitude = longitude,
  operator = operator,
  usageCost = usageCost,
  isOperational = isOperational,
  distanceKm = distanceKm,
  numberOfPoints = numberOfPoints,
)

fun ConnectorDto.toEntity(stationId: Long) = ConnectorEntity(
  stationId = stationId,
  typeName = typeName,
  formalName = formalName,
  powerKw = powerKw,
  amps = amps,
  voltage = voltage,
  currentType = currentType,
  isFastCharge = isFastCharge,
  isOperational = isOperational,
  quantity = quantity,
)