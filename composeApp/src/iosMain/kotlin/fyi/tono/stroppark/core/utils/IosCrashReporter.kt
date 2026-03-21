package fyi.tono.stroppark.core.utils

//import cocoapods.FirebaseCrashlytics.FIRCrashlytics

class IosCrashReporter : CrashReporter {
  override fun recordException(throwable: Throwable) {
    /*FIRCrashlytics.crashlytics().recordExceptionModel(

    )*/
  }

  override fun log(message: String) {}
    //FIRCrashlytics.crashlytics().log(message)

  override fun setCustomKey(key: String, value: String) {}
    //FIRCrashlytics.crashlytics().setCustomValue(value, forKey = key)
}