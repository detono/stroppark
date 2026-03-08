package fyi.tono.stroppark.core

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import fyi.tono.stroppark.core.database.StropParkDatabase

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<StropParkDatabase> {
  val appContext = context.applicationContext
  val dbFile = appContext.getDatabasePath("stropPark.db")
  return Room.databaseBuilder<StropParkDatabase>(
    context = appContext,
    name = dbFile.absolutePath,
  ).fallbackToDestructiveMigration(true)
}