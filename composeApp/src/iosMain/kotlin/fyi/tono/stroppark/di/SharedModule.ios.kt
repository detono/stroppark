package fyi.tono.stroppark.di

import androidx.room.RoomDatabase
import dev.icerock.moko.permissions.ios.PermissionsController
import fyi.tono.stroppark.core.database.StropParkDatabase
import fyi.tono.stroppark.core.database.getDatabaseBuilder
import fyi.tono.stroppark.core.location.LocationService
import fyi.tono.stroppark.core.location.LocationServiceImpl
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule = module {
  single<LocationService> { LocationServiceImpl() }

  single { PermissionsController() }
}
actual val platformDatabaseModule = module {
  single<RoomDatabase.Builder<StropParkDatabase>> {
    getDatabaseBuilder()
  }
}