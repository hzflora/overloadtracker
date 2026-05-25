package com.example.overloadtracker.ui

import com.example.overloadtracker.data.Exercise
import com.example.overloadtracker.data.WorkoutSession

data class DashboardUiState(
    val exercises: List<Exercise> = emptyList(),
    val sessions: List<com.example.overloadtracker.data.SessionWithSets> = emptyList(), // WorkoutSession yerine SessionWithSets oldu
    val totalWorkoutsThisWeek: Int = 0,
    val totalVolumeThisWeek: Double = 0.0,
    val lastWorkoutDate: String = "Antrenman bulunamadı",
    val isLoading: Boolean = true
)