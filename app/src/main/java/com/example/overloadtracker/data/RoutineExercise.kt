package com.example.overloadtracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routine_exercises")
data class RoutineExercise(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val routineId: Int, // Hangi programa (Routine) ait olduğu
    val name: String,   // Örn: "Bench Press"
    val setsAndReps: String, // Örn: "3 x 10"
    val restTime: String, // Örn: "90 Saniye"
    val orderIndex: Int = 0
)