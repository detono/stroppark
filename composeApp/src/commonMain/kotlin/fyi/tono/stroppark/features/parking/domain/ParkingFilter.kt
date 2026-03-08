package fyi.tono.stroppark.features.parking.domain

import org.jetbrains.compose.resources.StringResource
import stroppark.composeapp.generated.resources.Res
import stroppark.composeapp.generated.resources.parking_filter_available
import stroppark.composeapp.generated.resources.parking_filter_free
import stroppark.composeapp.generated.resources.parking_filter_lez
import stroppark.composeapp.generated.resources.parking_filter_lez_free

enum class ParkingFilter(val labelRes: StringResource) {
  AVAILABLE(Res.string.parking_filter_available),
  FREE(Res.string.parking_filter_free),
  LEZ(Res.string.parking_filter_lez),
  AVOID_LEZ(Res.string.parking_filter_lez_free)
}
