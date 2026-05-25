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

@Composable
fun MainScreen(viewModel: WorkoutViewModel) {
    val navController = rememberNavController()

    // Navigasyonun anlık durumunu dinliyoruz (Hangi sayfadayız?)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Alt menüyü sadece Aktif Antrenman ekranında DEĞİLSEK göster
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
                        selected = currentDestination?.hasRoute(Screen.History::class) == true,
                        onClick = {
                            navController.navigate(Screen.History) {
                                popUpTo(Screen.Dashboard)
                            }
                        },
                        label = { Text("Geçmiş") },
                        icon = { Icon(Icons.Default.DateRange, contentDescription = "Geçmiş") }
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
                    // Butona basıldığında çalışacak navigasyon tetikleyicisi
                    onStartWorkout = {
                        navController.navigate(Screen.ActiveWorkout)
                    }
                )
            }
            composable<Screen.History> {
                HistoryScreen(viewModel = viewModel)
            }
            composable<Screen.Exercises> {
                ExercisesScreen(viewModel = viewModel)
            }
            // Yeni Antrenman Ekranı Rotamız
            composable<Screen.ActiveWorkout> {
                ActiveWorkoutScreen(
                    viewModel = viewModel, // Bu satırı ekledik
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}