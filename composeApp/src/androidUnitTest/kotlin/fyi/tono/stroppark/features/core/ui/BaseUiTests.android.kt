package fyi.tono.stroppark.features.core.ui

import org.junit.runner.RunWith
import org.koin.mp.KoinPlatform.stopKoin
import org.robolectric.RobolectricTestRunner
import kotlin.test.AfterTest

@RunWith(RobolectricTestRunner::class)
actual abstract class BaseUiTests actual constructor() {
  @AfterTest
  fun tearDownKoin() {
    stopKoin()
  }
}