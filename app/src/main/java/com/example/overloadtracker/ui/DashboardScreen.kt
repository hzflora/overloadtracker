package com.example.overloadtracker.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.overloadtracker.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: WorkoutViewModel,
    onNavigateToActiveWorkout: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val brandRed = PremiumTheme.BrandRed
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(PremiumTheme.DeepSlate, Color.Black)
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Logo",
                            modifier = Modifier.size(32.dp).padding(end = 12.dp)
                        )
                        Text(
                            "OVERLOAD", 
                            fontWeight = FontWeight.Black, 
                            letterSpacing = 2.sp,
                            color = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {

            // --- WELCOME SECTION ---
            Column {
                Text(
                    text = "GÜNÜNÜ",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = brandRed
                )
                Text(
                    text = "DOMİNE ET",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 44.sp,
                    color = Color.White
                )
            }

            // --- METRICS ROW ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "HAFTALIK HACİM",
                    value = "${uiState.totalVolumeThisWeek.toInt()}",
                    unit = "kg",
                    icon = Icons.Rounded.LocalFireDepartment,
                    accentColor = brandRed
                )
                MetricCard(
                    modifier = Modifier.weight(1f),
                    title = "SON ANTRENMAN",
                    value = uiState.lastWorkoutDate.split(" ").firstOrNull() ?: "-",
                    unit = uiState.lastWorkoutDate.split(" ").getOrNull(1) ?: "",
                    icon = Icons.Rounded.CalendarToday,
                    accentColor = Color(0xFF4CAF50)
                )
            }

            // --- HERO START BUTTON ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(PremiumTheme.CornerLarge),
                colors = CardDefaults.cardColors(containerColor = PremiumTheme.Charcoal),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color.White.copy(alpha = 0.05f), Color.Transparent)
                            )
                        )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = brandRed.copy(alpha = 0.1f),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.FitnessCenter,
                                contentDescription = null,
                                modifier = Modifier.padding(16.dp).size(32.dp),
                                tint = brandRed
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "SINIRLARI ZORLA",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = Color.White
                        )

                        Text(
                            text = "Yeni bir rekor için hazır mısın?",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    val newSessionId = viewModel.createNewSession()
                                    onNavigateToActiveWorkout(newSessionId)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = brandRed,
                                contentColor = Color.White
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                        ) {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ANTRENMANI BAŞLAT",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp)) // Extra padding for custom bottom bar
        }
    }
}

@Composable
fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    accentColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = PremiumTheme.Charcoal),
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Icon(
                imageVector = icon, 
                contentDescription = null, 
                tint = accentColor, 
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = " $unit",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
