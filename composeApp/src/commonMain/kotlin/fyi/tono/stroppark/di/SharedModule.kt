package fyi.tono.stroppark.di

import fyi.tono.stroppark.core.network.createHttpClient
import fyi.tono.stroppark.features.chargers.ui.ChargerViewModel
import fyi.tono.stroppark.features.parking.ui.ParkingViewModel
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val networkModule = module {
  single { createHttpClient() }
}

val featureModule = module {
  // Repositories (Using single because we only need one instance)
  // singleOf(::ParkingRepositoryImpl)

  // ViewModels (Using factory because we want a fresh one per screen)
  factoryOf(::ParkingViewModel)
  factoryOf(::ChargerViewModel)
}

val sharedModules = listOf(networkModule, featureModule)