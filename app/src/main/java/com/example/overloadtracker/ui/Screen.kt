package com.example.overloadtracker.ui

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable object Dashboard : Screen
    @Serializable object Workouts : Screen // History -> Workouts olarak değişti
    @Serializable object Exercises : Screen
    @Serializable data class ActiveWorkout(val sessionId: Int = -1) : Screen
    @Serializable data class ExerciseDetail(val exerciseName: String) : Screen
}