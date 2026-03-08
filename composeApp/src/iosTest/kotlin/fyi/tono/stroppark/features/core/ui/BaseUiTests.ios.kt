package fyi.tono.stroppark.features.core.ui

import org.koin.mp.KoinPlatform.stopKoin
import kotlin.test.AfterTest

actual abstract class BaseUiTests actual constructor() {
  @AfterTest
  fun tearDownKoin() {
    stopKoin()
  }
}