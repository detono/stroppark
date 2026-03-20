package fyi.tono.stroppark.core.database

import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

fun getRoomDatabase(
  builder: RoomDatabase.Builder<StropParkDatabase>
): StropParkDatabase {
  return builder
    .setDriver(BundledSQLiteDriver())
    .setQueryCoroutineContext(Dispatchers.IO)
    .addMigrations(MIGRATION_2_3)
    .build()
}

val MIGRATION_2_3 = object : Migration(2, 3) {
  override fun migrate(connection: SQLiteConnection) {
    connection.execSQL(
      "CREATE TABLE IF NOT EXISTS sync_metadata (meta_key TEXT NOT NULL PRIMARY KEY, value TEXT NOT NULL)"
    )
  }
}