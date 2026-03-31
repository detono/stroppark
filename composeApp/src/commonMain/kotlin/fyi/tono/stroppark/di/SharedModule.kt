package fyi.tono.stroppark.di

import co.touchlab.kermit.Logger
import fyi.tono.stroppark.core.database.StropParkDatabase
import fyi.tono.stroppark.core.database.getRoomDatabase
import fyi.tono.stroppark.core.location.LocationPermissionService
import fyi.tono.stroppark.core.location.MokoLocationPermissionService
import fyi.tono.stroppark.core.network.createHttpClient
import fyi.tono.stroppark.features.chargers.data.ChargerRepositoryImpl
import fyi.tono.stroppark.features.chargers.domain.ChargerRepository
import fyi.tono.stroppark.features.chargers.ui.ChargerViewModel
import fyi.tono.stroppark.features.map.ui.MapViewModel
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
      dao = get()
    )
  }

  single<ChargerRepository> {
    ChargerRepositoryImpl(
      logger = get { parametersOf("ChargerRepositoryImpl") },
      httpClient = get(),
      dao = get(),
      crashReporter = get()
    )
  }
}

expect val platformModule: Module

val featureModule = module {
  // ViewModels (Using factory because we want a fresh one per screen)
  factory {
    ChargerViewModel(
      repository = get(),
      locationService = get(),
      locationPermission = get(),
      logger = get { parametersOf("ChargerViewModel") }
    )
  }
  factory {
    MapViewModel(
      parkingRepository = get(),
      chargerRepository = get(),
      locationService = get(),
      locationPermission = get(),
      logger = get { parametersOf("MapViewModel") }
    )
  }
  factoryOf(::ParkingViewModel)
}

val locationModule = module {
  single<LocationPermissionService> { MokoLocationPermissionService(get()) }
}
expect val platformDatabaseModule: Module

val dbModule = module {
  single<StropParkDatabase> {
    getRoomDatabase(get())
  }
  single { get<StropParkDatabase>().parkingDao }
  single { get<StropParkDatabase>().chargerDao }
}

val sharedModules = listOf(
  platformDatabaseModule,
  dbModule,
  networkModule,
  loggerModule,
  platformModule,
  dataModule,
  locationModule,
  featureModule,
)