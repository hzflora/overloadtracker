package com.example.overloadtracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_sessions")
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long,           // Tarihi milisaniye cinsinden tutacağız
    val bodyWeight: Double?,  // İsteğe bağlı vücut ağırlığı takibi
    val notes: String?        // "Bugün enerjim düşüktü" gibi notlar
)