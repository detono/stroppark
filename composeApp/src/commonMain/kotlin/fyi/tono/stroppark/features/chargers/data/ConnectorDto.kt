package fyi.tono.stroppark.features.chargers.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConnectorDto(
  @SerialName("type_name")
  val typeName: String? = null,
  @SerialName("formal_name")
  val formalName: String? = null,
  @SerialName("power_kw")
  val powerKw: Double? = null,
  val amps: Double? = null,
  val voltage: Double? = null,
  @SerialName("current_type")
  val currentType: String? = null,
  @SerialName("is_fast_charge")
  val isFastCharge: Boolean? = null,
  @SerialName("is_operational")
  val isOperational: Boolean? = null,
  val quantity: Int? = null
)