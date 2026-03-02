package fyi.tono.stroppark.features.parking.domain

import org.jetbrains.compose.resources.StringResource
import stroppark.composeapp.generated.resources.Res
import stroppark.composeapp.generated.resources.filter_available
import stroppark.composeapp.generated.resources.filter_free
import stroppark.composeapp.generated.resources.filter_lez
import stroppark.composeapp.generated.resources.filter_lez_free

enum class ParkingFilter(val labelRes: StringResource) {
  AVAILABLE(Res.string.filter_available),
  FREE(Res.string.filter_free),
  LEZ(Res.string.filter_lez),
  AVOID_LEZ(Res.string.filter_lez_free)
}
