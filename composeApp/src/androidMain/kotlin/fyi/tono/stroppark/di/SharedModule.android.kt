package fyi.tono.stroppark.di

import fyi.tono.stroppark.core.location.LocationService
import fyi.tono.stroppark.core.location.LocationServiceImpl
import org.koin.dsl.module

actual val platformModule = module {
  single<LocationService> {
    LocationServiceImpl(get())
  }
}