package com.example.overloadtracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,         // Örn: Bench Press
    val muscleGroup: String   // Örn: Göğüs
)