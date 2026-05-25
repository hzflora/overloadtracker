package com.example.overloadtracker.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute // Gerekli import

@Composable
fun MainScreen(viewModel: WorkoutViewModel) {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.hasRoute(Screen.ActiveWorkout::class) != true

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentDestination?.hasRoute(Screen.Dashboard::class) == true,
                        onClick = {
                            navController.navigate(Screen.Dashboard) {
                                popUpTo(Screen.Dashboard) { inclusive = false }
                            }
                        },
                        label = { Text("Dashboard") },
                        icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") }
                    )
                    NavigationBarItem(
                        selected = currentDestination?.hasRoute(Screen.Workouts::class) == true, // History -> Workouts yapıldı
                        onClick = {
                            navController.navigate(Screen.Workouts) {
                                popUpTo(Screen.Dashboard)
                            }
                        },
                        label = { Text("Antrenmanlar") }, // Etiket güncellendi
                        icon = { Icon(Icons.Default.DateRange, contentDescription = "Antrenmanlar") }
                    )
                    NavigationBarItem(
                        selected = currentDestination?.hasRoute(Screen.Exercises::class) == true,
                        onClick = {
                            navController.navigate(Screen.Exercises) {
                                popUpTo(Screen.Dashboard)
                            }
                        },
                        label = { Text("Egzersizler") },
                        icon = { Icon(Icons.Default.List, contentDescription = "Egzersizler") }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable<Screen.Dashboard> {
                DashboardScreen(
                    viewModel = viewModel,
                    onStartWorkout = {
                        // Yeni antrenman başlarken ID göndermeye gerek yok, varsayılan -1 olacak
                        navController.navigate(Screen.ActiveWorkout())
                    }
                )
            }
            composable<Screen.Workouts> {
                WorkoutsScreen(
                    viewModel = viewModel,
                    onNavigateToWorkout = { sessionId ->
                        // Tıklanan kartın ID'sini parametre olarak gönderiyoruz
                        navController.navigate(Screen.ActiveWorkout(sessionId = sessionId))
                    }
                )
            }
            composable<Screen.Exercises> {
                ExercisesScreen(viewModel = viewModel)
            }
            composable<Screen.ActiveWorkout> { backStackEntry ->
                // Gönderdiğimiz parametreyi yakalıyoruz
                val workoutRoute = backStackEntry.toRoute<Screen.ActiveWorkout>()

                ActiveWorkoutScreen(
                    viewModel = viewModel,
                    sessionId = workoutRoute.sessionId, // Yakalanan ID'yi ekrana iletiyoruz
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}