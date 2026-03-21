package fyi.tono.stroppark.fakes

import fyi.tono.stroppark.core.utils.CrashReporter

class FakeCrashReporter: CrashReporter {
  override fun recordException(throwable: Throwable) {
    println("$throwable")
  }

  override fun log(message: String) {
    println(message)
  }

  override fun setCustomKey(key: String, value: String) {
    println("$key: $value")
  }
}