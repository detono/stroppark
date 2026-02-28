package fyi.tono.stroppark.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class  GhentResponse<T: Any>(
  @SerialName("total_count") val totalCount: Int? = null,
  val results: List<T>
)