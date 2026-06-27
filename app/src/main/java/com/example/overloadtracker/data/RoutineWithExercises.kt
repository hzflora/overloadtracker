package com.example.overloadtracker.data

import androidx.room.Embedded
import androidx.room.Relation

data class RoutineWithExercises(
    @Embedded val routine: Routine,
    @Relation(
        parentColumn = "id",
        entityColumn = "routineId"
    )
    val exercises: List<RoutineExercise>
)