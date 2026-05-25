package com.example.overloadtracker.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

    // Sekmeler için state (0: Aktif, 1: Geçmiş)
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Aktif", "Geçmiş")

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
                        Text("Antrenmanlar", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Sekme Çubuğu
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            // Duruma göre listeyi filtreleme
            val filteredSessions = if (selectedTabIndex == 0) {
                uiState.sessions.filter { !it.session.isCompleted } // Aktif olanlar
            } else {
                uiState.sessions.filter { it.session.isCompleted }  // Bitenler
            }

            if (filteredSessions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (selectedTabIndex == 0) "Devam eden aktif bir antrenman yok." else "Geçmiş antrenman kaydınız bulunmuyor.",
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredSessions) { sessionWithSets ->
                        val dateString = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("tr"))
                            .format(Date(sessionWithSets.session.date))

                        val totalVolume = sessionWithSets.sets.sumOf { it.weight * it.reps }
                        val totalSets = sessionWithSets.sets.size

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // Karta tıklandığında ilgili oturumun ID'sini gönderiyoruz
                                    onNavigateToWorkout(sessionWithSets.session.id)
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = dateString, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Toplam Hacim: $totalVolume kg  |  Tamamlanan Set: $totalSets",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                // Aktif sekmesindeysek karta yönlendirici bir metin ekleyelim
                                if (selectedTabIndex == 0) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Devam Etmek İçin Dokun ➔",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}