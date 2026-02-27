package fyi.tono.stroppark

import android.app.Application
import fyi.tono.stroppark.di.sharedModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class StropParkApplication: Application() {
  override fun onCreate(){
    super.onCreate()

    startKoin {
      androidContext(this@StropParkApplication)
      modules(sharedModules)
    }
  }
}