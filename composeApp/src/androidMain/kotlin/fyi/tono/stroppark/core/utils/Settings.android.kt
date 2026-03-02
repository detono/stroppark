package fyi.tono.stroppark.core.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

actual fun openAppSettings() {
  object : KoinComponent {
    fun run() {
      val opener: AndroidSettingsOpener = get()
      opener.open()
    }
  }.run()
}

class AndroidSettingsOpener(private val context: Context) {
  fun open() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
      data = Uri.fromParts("package", context.packageName, null)
      flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(intent)
  }
}