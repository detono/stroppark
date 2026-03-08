package fyi.tono.stroppark.features.chargers.ui

import fyi.tono.stroppark.features.chargers.database.StationWithConnectors

data class ChargerUiModel(
  val id: Long,
  val name: String,
  val address: String,
  val latitude: Double?,
  val longitude: Double?,
  val operator: String?,
  val usageCost: String?,
  val isOperational: Boolean,
  val numberOfPoints: Int?,
  val distanceKm: Double?,
  val fastestChargerKw: Double?,
  val connectorSummary: String,
  val hasFastCharge: Boolean,
)

fun StationWithConnectors.toUiModel() = ChargerUiModel(
  id = station.id,
  name = station.name ?: station.address ?: "Unknown",
  address = station.address ?: "",
  latitude = station.latitude,
  longitude = station.longitude,
  operator = station.operator,
  usageCost = station.usageCost,
  isOperational = station.isOperational ?: true,
  numberOfPoints = station.numberOfPoints,
  distanceKm = station.distanceKm,
  fastestChargerKw = connectors.mapNotNull { it.powerKw }.maxOrNull(),
  connectorSummary = connectors
    .groupBy { it.typeName }
    .entries
    .joinToString(" • ") { (type, list) ->
      "${list.sumOf { it.quantity ?: 1 }}x ${type ?: "Unknown"}"
    },
  hasFastCharge = connectors.any { (it.powerKw ?: 0.0) >= 50.0 },
)