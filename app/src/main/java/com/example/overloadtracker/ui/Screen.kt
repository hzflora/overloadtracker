package com.example.overloadtracker.ui

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable object Dashboard : Screen
    @Serializable object History : Screen
    @Serializable object Exercises : Screen
    @Serializable object ActiveWorkout : Screen // Yeni rotamız
}