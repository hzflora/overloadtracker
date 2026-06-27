package com.example.overloadtracker.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute

@Composable
fun MainScreen(viewModel: WorkoutViewModel) {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination?.hasRoute(Screen.ActiveWorkout::class) != true && 
                      currentDestination?.hasRoute(Screen.ExerciseDetail::class) != true

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                CustomPremiumBottomBar(
                    currentDestination = currentDestination,
                    onNavigate = { screen ->
                        navController.navigate(screen) {
                            popUpTo(Screen.Dashboard) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        containerColor = Color.Black
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard,
            modifier = Modifier.padding(bottom = if (showBottomBar) 0.dp else 0.dp) // We handle padding inside screens or custom
        ) {
            composable<Screen.Dashboard> {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToActiveWorkout = { sessionId ->
                        navController.navigate(Screen.ActiveWorkout(sessionId = sessionId))
                    }
                )
            }
            composable<Screen.Workouts> {
                WorkoutsScreen(
                    viewModel = viewModel,
                    onNavigateToWorkout = { sessionId ->
                        navController.navigate(Screen.ActiveWorkout(sessionId = sessionId))
                    }
                )
            }
            composable<Screen.Exercises> {
                ExercisesScreen(
                    viewModel = viewModel,
                    onNavigateToExerciseDetail = { name ->
                        navController.navigate(Screen.ExerciseDetail(exerciseName = name))
                    }
                )
            }
            composable<Screen.ActiveWorkout> { backStackEntry ->
                val workoutRoute = backStackEntry.toRoute<Screen.ActiveWorkout>()
                ActiveWorkoutScreen(
                    viewModel = viewModel,
                    sessionId = workoutRoute.sessionId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToExerciseDetail = { name ->
                        navController.navigate(Screen.ExerciseDetail(exerciseName = name))
                    }
                )
            }
            composable<Screen.ExerciseDetail> { backStackEntry ->
                val detailRoute = backStackEntry.toRoute<Screen.ExerciseDetail>()
                ExerciseDetailScreen(
                    exerciseName = detailRoute.exerciseName,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun CustomPremiumBottomBar(
    currentDestination: androidx.navigation.NavDestination?,
    onNavigate: (Screen) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            shape = RoundedCornerShape(24.dp),
            color = PremiumTheme.Charcoal.copy(alpha = 0.95f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(
                    selected = currentDestination?.hasRoute(Screen.Dashboard::class) == true,
                    icon = Icons.Default.Home,
                    label = "DASHBOARD",
                    onClick = { onNavigate(Screen.Dashboard) }
                )
                BottomNavItem(
                    selected = currentDestination?.hasRoute(Screen.Workouts::class) == true,
                    icon = Icons.Default.DateRange,
                    label = "LİSTE",
                    onClick = { onNavigate(Screen.Workouts) }
                )
                BottomNavItem(
                    selected = currentDestination?.hasRoute(Screen.Exercises::class) == true,
                    icon = Icons.Default.List,
                    label = "PROGRAM",
                    onClick = { onNavigate(Screen.Exercises) }
                )
            }
        }
    }
}

@Composable
fun RowScope.BottomNavItem(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val contentColor by animateColorAsState(
        targetValue = if (selected) PremiumTheme.BrandRed else Color.Gray,
        label = "color"
    )
    
    val indicatorWidth by animateDpAsState(
        targetValue = if (selected) 32.dp else 0.dp,
        label = "width"
    )

    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp,
            color = contentColor
        )
        
        Spacer(modifier = Modifier.height(6.dp))
        
        // Custom selection indicator
        Box(
            modifier = Modifier
                .width(indicatorWidth)
                .height(2.dp)
                .clip(CircleShape)
                .background(contentColor)
        )
    }
}
