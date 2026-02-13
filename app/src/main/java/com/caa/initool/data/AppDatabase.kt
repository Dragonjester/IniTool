package com.caa.initool.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.caa.initool.model.Encounter
import com.caa.initool.model.Gegner
import com.caa.initool.model.Player

@Database(
    entities = [Player::class, Encounter::class, Gegner::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun playerDao(): PlayerDao
    abstract fun encounterDao(): EncounterDao
    abstract fun gegnerDao(): GegnerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `encounters` (" +
                        "`id` TEXT NOT NULL, " +
                        "`name` TEXT NOT NULL, " +
                        "PRIMARY KEY(`id`))"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `gegner` (" +
                        "`id` TEXT NOT NULL, " +
                        "`encounterId` TEXT NOT NULL, " +
                        "`name` TEXT NOT NULL, " +
                        "`ini` INTEGER NOT NULL, " +
                        "`minus4` INTEGER NOT NULL, " +
                        "`minus8` INTEGER NOT NULL, " +
                        "`twoD6` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`id`), " +
                        "FOREIGN KEY(`encounterId`) REFERENCES `encounters`(`id`) ON DELETE CASCADE)"
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_gegner_encounterId` ON `gegner` (`encounterId`)")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "initool.db"
                ).addMigrations(MIGRATION_1_2).build().also { INSTANCE = it }
            }
        }
    }
}
