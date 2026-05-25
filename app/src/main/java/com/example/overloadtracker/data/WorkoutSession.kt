package com.example.overloadtracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_sessions")
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long,
    val bodyWeight: Double?,
    val notes: String?,
    val isCompleted: Boolean = false // YENİ: Antrenmanın aktif mi geçmiş mi olduğunu belirler
)