package fyi.tono.stroppark.di

import fyi.tono.stroppark.features.chargers.ui.ChargerViewModel
import fyi.tono.stroppark.features.parking.ui.ParkingViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val networkModule = module {
  single {
    HttpClient {
      install(ContentNegotiation) {
        json(Json {
          ignoreUnknownKeys = true
          prettyPrint = true
        })
      }
    }
  }
}

val featureModule = module {
  // Repositories (Using single because we only need one instance)
  // singleOf(::ParkingRepositoryImpl)

  // ViewModels (Using factory because we want a fresh one per screen)
  factoryOf(::ParkingViewModel)
  factoryOf(::ChargerViewModel)
}

val sharedModules = listOf(networkModule, featureModule)