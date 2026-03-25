package fyi.tono.stroppark.core.utils

data class SyncProgress(
  val loaded: Int,
  val total: Int,
  val done: Boolean = false
)
