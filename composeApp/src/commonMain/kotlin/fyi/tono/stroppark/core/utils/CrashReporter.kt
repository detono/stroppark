package fyi.tono.stroppark.core.utils

interface CrashReporter {
  fun recordException(throwable: Throwable)
  fun log(message: String)
  fun setCustomKey(key: String, value: String)
}