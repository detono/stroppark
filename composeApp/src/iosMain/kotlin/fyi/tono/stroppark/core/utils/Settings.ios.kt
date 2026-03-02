package fyi.tono.stroppark.core.utils

import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString

actual fun openAppSettings() {
  UIApplication.sharedApplication.openURL(
    NSURL.URLWithString(UIApplicationOpenSettingsURLString)!!
  )
}