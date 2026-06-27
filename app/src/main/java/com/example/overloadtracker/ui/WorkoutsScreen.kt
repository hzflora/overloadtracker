package com.example.overloadtracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutsScreen(viewModel: WorkoutViewModel, onNavigateToWorkout: (Int) -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val routines by viewModel.routines.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("AKTİF", "GEÇMİŞ")

    val brandRed = PremiumTheme.BrandRed

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "ANTRENMANLAR", 
                        fontWeight = FontWeight.Black, 
                        letterSpacing = 1.sp,
                        fontSize = 20.sp
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Black,
                contentColor = brandRed,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = brandRed,
                        height = 3.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                title,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                fontSize = 13.sp,
                                color = if (selectedTabIndex == index) brandRed else Color.Gray
                            )
                        }
                    )
                }
            }

            val filteredSessions = if (selectedTabIndex == 0) {
                uiState.sessions.filter { !it.session.isCompleted }
            } else {
                uiState.sessions.filter { it.session.isCompleted }
            }

            if (filteredSessions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.FitnessCenter,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = PremiumTheme.Charcoal
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (selectedTabIndex == 0) "Aktif antrenman yok." else "Geçmiş kaydınız yok.",
                            color = Color.Gray,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp)
                ) {
                    items(filteredSessions) { sessionWithSets ->
                        val dateString = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("tr")).format(Date(sessionWithSets.session.date))
                        val totalVolume = sessionWithSets.sets.sumOf { it.weight * it.reps }
                        val totalSets = sessionWithSets.sets.size

                        val firstExerciseName = sessionWithSets.sets.firstOrNull()?.exerciseName
                        val detectedSplit = if (firstExerciseName != null) {
                            val splitKey = routines.find { it.exercises.any { ex -> ex.name == firstExerciseName } }?.routine?.name
                            if (splitKey != null) splitKey.uppercase() else "SERBEST"
                        } else {
                            "BOŞ"
                        }

                        WorkoutPremiumCard(
                            title = detectedSplit,
                            date = dateString,
                            volume = totalVolume.toInt().toString(),
                            sets = totalSets.toString(),
                            isActive = selectedTabIndex == 0,
                            brandRed = brandRed,
                            onClick = { onNavigateToWorkout(sessionWithSets.session.id) },
                            onDelete = { viewModel.deleteSession(sessionWithSets.session.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WorkoutPremiumCard(
    title: String,
    date: String,
    volume: String,
    sets: String,
    isActive: Boolean,
    brandRed: Color,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PremiumTheme.Charcoal),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        if (isActive) brandRed.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isActive) Icons.Rounded.FitnessCenter else Icons.Rounded.CalendarToday,
                    contentDescription = null,
                    tint = if (isActive) brandRed else Color.Gray,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = Color.White
                )
                Text(
                    text = date,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 2.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$volume kg",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = brandRed
                    )
                    Text(
                        text = " • $sets SET",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Sil",
                    tint = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
