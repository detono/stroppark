package fyi.tono.stroppark.core.utils

import com.google.firebase.crashlytics.FirebaseCrashlytics

class AndroidCrashReporter : CrashReporter {
  private val crashlytics = FirebaseCrashlytics.getInstance()

  override fun recordException(throwable: Throwable) =
    crashlytics.recordException(throwable)

  override fun log(message: String) =
    crashlytics.log(message)

  override fun setCustomKey(key: String, value: String) =
    crashlytics.setCustomKey(key, value)
}