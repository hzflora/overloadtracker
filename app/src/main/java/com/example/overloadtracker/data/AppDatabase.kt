package com.example.overloadtracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Exercise::class, WorkoutSession::class, WorkoutSet::class, Routine::class, RoutineExercise::class],
    version = 3, // VERSİYON 3 OLDU
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `routines` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `routine_exercises` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `routineId` INTEGER NOT NULL, `name` TEXT NOT NULL, `setsAndReps` TEXT NOT NULL, `restTime` TEXT NOT NULL)")
            }
        }

        // YENİ: 2'den 3'e geçişte sıra numarası sütunu ekliyoruz.
        // Eski hareketlerin sırası bozulmasın diye "orderIndex = id" yapıyoruz!
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE routine_exercises ADD COLUMN orderIndex INTEGER NOT NULL DEFAULT 0")
                db.execSQL("UPDATE routine_exercises SET orderIndex = id")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "overload_database",
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3) // BURAYA EKLENDİ
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}