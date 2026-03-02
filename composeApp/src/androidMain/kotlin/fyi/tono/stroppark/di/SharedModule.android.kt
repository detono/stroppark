package fyi.tono.stroppark.di

import android.content.Context
import androidx.room.RoomDatabase
import dev.icerock.moko.permissions.PermissionsController
import fyi.tono.stroppark.core.database.StropParkDatabase
import fyi.tono.stroppark.core.getDatabaseBuilder
import fyi.tono.stroppark.core.location.LocationService
import fyi.tono.stroppark.core.location.LocationServiceImpl
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule = module {
  single<LocationService> {
    LocationServiceImpl(get())
  }

  single { PermissionsController(applicationContext = androidApplication()) }
}
actual val platformDatabaseModule = module {
  single<RoomDatabase.Builder<StropParkDatabase>> {
    getDatabaseBuilder(get())
  }
}