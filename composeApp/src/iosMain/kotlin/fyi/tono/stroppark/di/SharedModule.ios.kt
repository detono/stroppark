package fyi.tono.stroppark.di

import dev.icerock.moko.permissions.ios.PermissionsController
import fyi.tono.stroppark.core.location.LocationService
import fyi.tono.stroppark.core.location.LocationServiceImpl
import org.koin.dsl.module

actual val platformModule = module {
  single<LocationService> { LocationServiceImpl() }

  single { PermissionsController() }
}