package com.example.overloadtracker.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    val tabs = listOf("Aktif", "Geçmiş")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Antrenmanlar", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontWeight = FontWeight.Bold) }
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
                    Text(text = if (selectedTabIndex == 0) "Devam eden aktif bir antrenman yok." else "Geçmiş antrenman kaydınız bulunmuyor.", color = MaterialTheme.colorScheme.secondary)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filteredSessions) { sessionWithSets ->
                        val dateString = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("tr")).format(Date(sessionWithSets.session.date))
                        val totalVolume = sessionWithSets.sets.sumOf { it.weight * it.reps }
                        val totalSets = sessionWithSets.sets.size

                        val firstExerciseName = sessionWithSets.sets.firstOrNull()?.exerciseName
                        val detectedSplit = if (firstExerciseName != null) {
                            val splitKey = routines.find { it.exercises.any { ex -> ex.name == firstExerciseName } }?.routine?.name
                            if (splitKey != null) "$splitKey Antrenmanı" else "Serbest Antrenman"
                        } else {
                            "Boş Antrenman"
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { onNavigateToWorkout(sessionWithSets.session.id) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = detectedSplit, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = dateString, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "Toplam Hacim: $totalVolume kg  |  Set: $totalSets", fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
                                    if (selectedTabIndex == 0) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(text = "Devam Etmek İçin Dokun ➔", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                                IconButton(onClick = { viewModel.deleteSession(sessionWithSets.session.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Antrenmanı Sil", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}