package fyi.tono.stroppark.features.chargers.domain

import org.jetbrains.compose.resources.StringResource
import stroppark.composeapp.generated.resources.Res
import stroppark.composeapp.generated.resources.charger_filter_fast_charge
import stroppark.composeapp.generated.resources.charger_filter_free
import stroppark.composeapp.generated.resources.charger_filter_kw_150
import stroppark.composeapp.generated.resources.charger_filter_kw_22
import stroppark.composeapp.generated.resources.charger_filter_kw_50

enum class ChargerFilter (val labelRes: StringResource) {

  FREE(Res.string.charger_filter_free),
  FAST_CHARGE(Res.string.charger_filter_fast_charge),
  KW_22(Res.string.charger_filter_kw_22),
  KW_50(Res.string.charger_filter_kw_50),
  KW_150(Res.string.charger_filter_kw_150),

}
