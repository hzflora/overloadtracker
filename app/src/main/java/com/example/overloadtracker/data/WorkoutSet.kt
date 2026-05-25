package com.example.overloadtracker.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

// 1. Güncellenen Set Tablosu
@Entity(tableName = "workout_sets")
data class WorkoutSet(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: Int,
    val exerciseName: String, // YENİ: Artık ID yerine hareketin adını kaydediyoruz
    val weight: Double,
    val reps: Int,
    val setNumber: Int
)

// 2. YENİ: Oturum ve Setleri birbirine bağlayan ilişkisel veri sınıfı
data class SessionWithSets(
    @Embedded val session: WorkoutSession,
    @Relation(
        parentColumn = "id",
        entityColumn = "sessionId"
    )
    val sets: List<WorkoutSet>
)