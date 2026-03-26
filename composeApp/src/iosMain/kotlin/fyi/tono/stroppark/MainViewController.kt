package fyi.tono.stroppark

import androidx.compose.ui.window.ComposeUIViewController
import fyi.tono.stroppark.di.sharedModules
import org.koin.core.context.startKoin

fun MainViewController() = ComposeUIViewController(
  configure = {
    initKoin()
    GMSServices.provideAPIKey(BuildKonfig.MAPS_API_KEY)
  },
  content = {
    App()
  }
)

fun initKoin() {
  startKoin {
    modules(sharedModules)
  }
}