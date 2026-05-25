package com.example.overloadtracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisesScreen(viewModel: WorkoutViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Antrenman Programı", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // ActiveWorkoutScreen.kt dosyasındaki workoutSplits haritasını okuyoruz
            workoutSplits.forEach { (splitName, exercises) ->
                // Gün Başlıkları (PUSH GÜNÜ, PULL GÜNÜ vb.)
                item {
                    Text(
                        text = "$splitName GÜNÜ",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                    )
                }

                // O güne ait hareketlerin kartları
                items(exercises) { exercise ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = exercise.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            // Dinlenme Süresi Rozeti
                            AssistChip(
                                onClick = { },
                                label = { Text(exercise.restTime, fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            )
                        }
                    }
                }
            }
            // Alt navigasyon barı üstüne binmesin diye boşluk
            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}