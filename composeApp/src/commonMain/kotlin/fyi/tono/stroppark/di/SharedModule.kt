package fyi.tono.stroppark.di

import co.touchlab.kermit.Logger
import fyi.tono.stroppark.core.network.createHttpClient
import fyi.tono.stroppark.features.chargers.data.ChargerRepositoryImpl
import fyi.tono.stroppark.features.chargers.domain.ChargerRepository
import fyi.tono.stroppark.features.chargers.ui.ChargerViewModel
import fyi.tono.stroppark.features.parking.data.ParkingRepositoryImpl
import fyi.tono.stroppark.features.parking.domain.ParkingRepository
import fyi.tono.stroppark.features.parking.ui.ParkingViewModel
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val networkModule = module {
  single {
    Json {
      ignoreUnknownKeys = true
      isLenient = true
      prettyPrint = true
    }
  }

  single { createHttpClient(get()) }
}

val loggerModule = module {
  factory { (tag: String) -> Logger.withTag(tag) }
}

val dataModule = module {
  // Repositories (Using single because we only need one instance)
  single<ParkingRepository> {
    ParkingRepositoryImpl(
      logger = get { parametersOf("ParkingRepositoryImpl") },
      httpClient = get(),
      json = get(),
    )
  }

  single<ChargerRepository> {
    ChargerRepositoryImpl(
      logger = get { parametersOf("ChargerRepositoryImpl") },
      httpClient = get(),
    )
  }
}

expect val platformModule: Module

val featureModule = module {
  // ViewModels (Using factory because we want a fresh one per screen)
  factoryOf(::ParkingViewModel)
  factoryOf(::ChargerViewModel)
}

val sharedModules = listOf(
  networkModule,
  loggerModule,
  platformModule,
  dataModule,
  featureModule,
)