package fyi.tono.stroppark.features.map.domain

import org.jetbrains.compose.resources.StringResource
import stroppark.composeapp.generated.resources.Res
import stroppark.composeapp.generated.resources.charger_filter_fast_charge
import stroppark.composeapp.generated.resources.charger_filter_free
import stroppark.composeapp.generated.resources.charger_filter_kw_150
import stroppark.composeapp.generated.resources.charger_filter_kw_22
import stroppark.composeapp.generated.resources.charger_filter_kw_50
import stroppark.composeapp.generated.resources.map_filter_chargers
import stroppark.composeapp.generated.resources.map_filter_parking

enum class MapFilter (val labelRes: StringResource) {
  CHARGERS(Res.string.map_filter_chargers),
  PARKING(Res.string.map_filter_parking),
}
