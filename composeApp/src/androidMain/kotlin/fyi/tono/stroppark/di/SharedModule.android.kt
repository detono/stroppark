package fyi.tono.stroppark.di

import android.content.Context
import dev.icerock.moko.permissions.PermissionsController
import fyi.tono.stroppark.core.location.LocationService
import fyi.tono.stroppark.core.location.LocationServiceImpl
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val platformModule = module {
  single<LocationService> {
    LocationServiceImpl(get())
  }

  single { PermissionsController(applicationContext = androidApplication()) }
}