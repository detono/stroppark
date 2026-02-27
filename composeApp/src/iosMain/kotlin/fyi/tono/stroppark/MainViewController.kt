package fyi.tono.stroppark

import androidx.compose.ui.window.ComposeUIViewController
import fyi.tono.stroppark.di.sharedModules
import org.koin.core.context.startKoin

fun MainViewController() = ComposeUIViewController(
  configure = { initKoin() },
  content = {
    App()
  }
)

fun initKoin() {
  startKoin {
    modules(sharedModules)
  }
}