package com.example.overloadtracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Exercise::class, WorkoutSession::class, WorkoutSet::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {

    // Veritabanı üzerinden DAO'ya erişim sağlayacağımız fonksiyon
    abstract fun workoutDao(): WorkoutDao

    // Singleton Pattern: Veritabanı bağlantısının uygulama boyunca sadece bir kez açılmasını sağlar
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // Eğer INSTANCE null değilse onu döndür, null ise yeni bir veritabanı inşa et
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "overload_database",
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}