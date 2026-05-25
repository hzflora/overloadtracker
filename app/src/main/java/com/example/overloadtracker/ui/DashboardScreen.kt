package com.example.overloadtracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.overloadtracker.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: WorkoutViewModel, onStartWorkout: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    androidx.compose.foundation.layout.Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = com.example.overloadtracker.R.drawable.logo),
                            contentDescription = "Logo",
                            modifier = Modifier.size(32.dp).padding(end = 8.dp) // Yazıyla arasına boşluk verdik
                        )
                        Text("Overload Tracker", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onStartWorkout,
                icon = { Icon(Icons.Default.FitnessCenter, contentDescription = "Antrenman Başlat") },
                text = { Text("Antrenmana Başla") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Sadece Özet Kartları Kaldı
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Haftalık Hacim",
                    value = "${uiState.totalVolumeThisWeek} kg"
                )
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    title = "Son Antrenman",
                    value = uiState.lastWorkoutDate
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Liste yerine şık bir yönlendirme mesajı
            Box(
                modifier = Modifier.fillMaxSize().padding(bottom = 60.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.example.overloadtracker.R.drawable.logo),
                        contentDescription = "Overload Tracker Logo",
                        modifier = Modifier.size(80.dp) // İstersen boyutu büyütebilirsin
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "İlerlemeyi kaydetmek için\nyeni bir antrenman başlat.",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryCard(modifier: Modifier = Modifier, title: String, value: String) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}