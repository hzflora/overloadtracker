package com.example.overloadtracker.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    exerciseName: String,
    onNavigateBack: () -> Unit
) {
    val exerciseInfo = remember(exerciseName) { ExerciseInfoGenerator.generate(exerciseName) }
    val brandRed = PremiumTheme.BrandRed

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        exerciseName.uppercase(), 
                        fontWeight = FontWeight.Black, 
                        letterSpacing = 1.sp,
                        fontSize = 18.sp
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = Color.White)
                    }
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
                .background(Color.Black)
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            
            RealisticMuscleVisualization(targetRegions = exerciseInfo.targetRegions, activeColor = brandRed)

            PremiumInfoSection(
                title = "HAKKINDA",
                icon = Icons.Rounded.Info,
                iconColor = brandRed
            ) {
                Text(
                    text = exerciseInfo.description,
                    fontSize = 15.sp,
                    lineHeight = 24.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }

            PremiumInfoSection(
                title = "HEDEF KASLAR",
                icon = Icons.Rounded.Bolt,
                iconColor = Color(0xFF4CAF50)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    MuscleGroupPremium(label = "BİRİNCİL", muscles = exerciseInfo.primaryMuscles, color = brandRed)
                    if (exerciseInfo.secondaryMuscles.isNotEmpty()) {
                        MuscleGroupPremium(label = "İKİNCİL", muscles = exerciseInfo.secondaryMuscles, color = Color.Gray)
                    }
                }
            }

            PremiumInfoSection(
                title = "NASIL YAPILIR?",
                icon = Icons.Rounded.Psychology,
                iconColor = PremiumTheme.ElectricBlue
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    exerciseInfo.instructions.forEachIndexed { index, instruction ->
                        Row(verticalAlignment = Alignment.Top) {
                            Text(
                                text = "${index + 1}",
                                fontWeight = FontWeight.Black,
                                color = brandRed,
                                fontSize = 18.sp,
                                modifier = Modifier.width(32.dp)
                            )
                            Text(
                                text = instruction,
                                fontSize = 15.sp,
                                color = Color.White,
                                lineHeight = 22.sp
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PremiumTheme.Charcoal),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("ZORLUK SEVİYESİ", fontWeight = FontWeight.Black, letterSpacing = 1.sp, color = Color.Gray, fontSize = 12.sp)
                    Surface(
                        color = brandRed.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, brandRed.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = exerciseInfo.difficulty.uppercase(),
                            color = brandRed,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(120.dp)) // Extra space for floating nav
        }
    }
}

@Composable
fun RealisticMuscleVisualization(targetRegions: List<MuscleRegion>, activeColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(380.dp),
        shape = RoundedCornerShape(PremiumTheme.CornerLarge),
        colors = CardDefaults.cardColors(containerColor = PremiumTheme.Charcoal),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MuscleModelContainer(isFront = true, targetRegions = targetRegions, highlightColor = activeColor, pulseAlpha = alpha)
            Box(modifier = Modifier.width(1.dp).fillMaxHeight(0.6f).background(Color.White.copy(alpha = 0.05f)))
            MuscleModelContainer(isFront = false, targetRegions = targetRegions, highlightColor = activeColor, pulseAlpha = alpha)
        }
    }
}

@Composable
fun MuscleModelContainer(isFront: Boolean, targetRegions: List<MuscleRegion>, highlightColor: Color, pulseAlpha: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = if (isFront) "ÖN CEPHE" else "ARKA CEPHE",
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = Color.Gray,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(20.dp))
        Canvas(modifier = Modifier.size(130.dp, 280.dp)) {
            val scale = size.width / 100f
            drawHighFidelitySilhouette(isFront, scale)
            drawVolumetricShading(isFront, scale)
            targetRegions.forEach { region ->
                drawRealisticMuscle(region, isFront, highlightColor, pulseAlpha, scale)
            }
        }
    }
}

fun DrawScope.drawHighFidelitySilhouette(isFront: Boolean, scale: Float) {
    val bodyPath = Path().apply {
        addOval(androidx.compose.ui.geometry.Rect(38f * scale, 5f * scale, 62f * scale, 35f * scale))
        moveTo(32f * scale, 40f * scale)
        quadraticTo(50f * scale, 32f * scale, 68f * scale, 40f * scale)
        quadraticTo(85f * scale, 45f * scale, 82f * scale, 75f * scale)
        quadraticTo(78f * scale, 100f * scale, 65f * scale, 110f * scale)
        lineTo(35f * scale, 110f * scale)
        quadraticTo(22f * scale, 100f * scale, 18f * scale, 75f * scale)
        quadraticTo(15f * scale, 45f * scale, 32f * scale, 40f * scale)
        close()

        moveTo(22f * scale, 55f * scale)
        quadraticTo(5f * scale, 65f * scale, 12f * scale, 95f * scale)
        quadraticTo(15f * scale, 120f * scale, 22f * scale, 150f * scale)
        lineTo(30f * scale, 150f * scale)
        quadraticTo(28f * scale, 120f * scale, 32f * scale, 95f * scale)
        close()
        
        moveTo(78f * scale, 55f * scale)
        quadraticTo(95f * scale, 65f * scale, 88f * scale, 95f * scale)
        quadraticTo(85f * scale, 120f * scale, 78f * scale, 150f * scale)
        lineTo(70f * scale, 150f * scale)
        quadraticTo(72f * scale, 120f * scale, 68f * scale, 95f * scale)
        close()

        moveTo(35f * scale, 110f * scale)
        quadraticTo(28f * scale, 150f * scale, 34f * scale, 210f * scale)
        quadraticTo(32f * scale, 240f * scale, 38f * scale, 270f * scale)
        lineTo(48f * scale, 270f * scale)
        quadraticTo(50f * scale, 210f * scale, 48f * scale, 110f * scale)
        close()
        
        moveTo(65f * scale, 110f * scale)
        quadraticTo(72f * scale, 150f * scale, 66f * scale, 210f * scale)
        quadraticTo(68f * scale, 240f * scale, 62f * scale, 270f * scale)
        lineTo(52f * scale, 270f * scale)
        quadraticTo(50f * scale, 210f * scale, 52f * scale, 110f * scale)
        close()
    }
    drawPath(bodyPath, Color(0xFF121212))
    drawPath(bodyPath, Color.White.copy(alpha = 0.1f), style = Stroke(width = 1.2f))
}

fun DrawScope.drawVolumetricShading(isFront: Boolean, scale: Float) {
    val highlightBrush = Brush.radialGradient(
        colors = listOf(Color.White.copy(alpha = 0.03f), Color.Transparent),
        center = Offset(50f * scale, 80f * scale),
        radius = 120f * scale
    )
    drawRect(highlightBrush, size = Size(size.width, size.height))
}

fun DrawScope.drawRealisticMuscle(region: MuscleRegion, isFront: Boolean, color: Color, alpha: Float, scale: Float) {
    val musclePath = Path().apply {
        when (region) {
            MuscleRegion.CHEST -> if (isFront) {
                addOval(androidx.compose.ui.geometry.Rect(32f * scale, 48f * scale, 50f * scale, 75f * scale))
                addOval(androidx.compose.ui.geometry.Rect(50f * scale, 48f * scale, 68f * scale, 75f * scale))
            }
            MuscleRegion.BACK -> if (!isFront) {
                moveTo(32f * scale, 42f * scale)
                quadraticTo(50f * scale, 38f * scale, 68f * scale, 42f * scale)
                lineTo(62f * scale, 105f * scale)
                lineTo(38f * scale, 105f * scale)
                close()
            }
            MuscleRegion.SHOULDERS -> {
                addOval(androidx.compose.ui.geometry.Rect(15f * scale, 45f * scale, 34f * scale, 75f * scale))
                addOval(androidx.compose.ui.geometry.Rect(66f * scale, 45f * scale, 85f * scale, 75f * scale))
            }
            MuscleRegion.BICEPS -> if (isFront) {
                addOval(androidx.compose.ui.geometry.Rect(16f * scale, 78f * scale, 28f * scale, 110f * scale))
                addOval(androidx.compose.ui.geometry.Rect(72f * scale, 78f * scale, 84f * scale, 110f * scale))
            }
            MuscleRegion.TRICEPS -> if (!isFront) {
                addOval(androidx.compose.ui.geometry.Rect(15f * scale, 75f * scale, 28f * scale, 120f * scale))
                addOval(androidx.compose.ui.geometry.Rect(72f * scale, 75f * scale, 85f * scale, 120f * scale))
            }
            MuscleRegion.QUADS -> if (isFront) {
                addOval(androidx.compose.ui.geometry.Rect(34f * scale, 115f * scale, 48f * scale, 190f * scale))
                addOval(androidx.compose.ui.geometry.Rect(52f * scale, 115f * scale, 66f * scale, 190f * scale))
            }
            MuscleRegion.HAMSTRINGS -> if (!isFront) {
                addOval(androidx.compose.ui.geometry.Rect(34f * scale, 120f * scale, 48f * scale, 195f * scale))
                addOval(androidx.compose.ui.geometry.Rect(52f * scale, 120f * scale, 66f * scale, 195f * scale))
            }
            MuscleRegion.CALVES -> {
                addOval(androidx.compose.ui.geometry.Rect(34f * scale, 220f * scale, 47f * scale, 260f * scale))
                addOval(androidx.compose.ui.geometry.Rect(53f * scale, 220f * scale, 66f * scale, 260f * scale))
            }
            MuscleRegion.ABS -> if (isFront) {
                addRect(androidx.compose.ui.geometry.Rect(40f * scale, 75f * scale, 60f * scale, 105f * scale))
            }
            MuscleRegion.GLUTES -> if (!isFront) {
                addOval(androidx.compose.ui.geometry.Rect(35f * scale, 105f * scale, 65f * scale, 140f * scale))
            }
            MuscleRegion.TRAPS -> {
                moveTo(42f * scale, 40f * scale)
                lineTo(58f * scale, 40f * scale)
                lineTo(54f * scale, 34f * scale)
                lineTo(46f * scale, 34f * scale)
                close()
            }
            else -> {}
        }
    }

    if (!musclePath.isEmpty) {
        val bounds = musclePath.getBounds()
        drawPath(musclePath, Color.Black.copy(alpha = 0.5f))
        drawPath(
            path = musclePath,
            brush = Brush.radialGradient(
                colors = listOf(color.copy(alpha = alpha), color.copy(alpha = alpha * 0.3f)),
                center = bounds.center,
                radius = bounds.width.coerceAtLeast(bounds.height)
            )
        )
        drawPath(
            path = musclePath,
            color = Color.White.copy(alpha = alpha * 0.4f),
            style = Stroke(width = 1.2f)
        )
    }
}

@Composable
fun PremiumInfoSection(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = iconColor.copy(alpha = 0.15f),
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = icon, 
                    contentDescription = null, 
                    tint = iconColor, 
                    modifier = Modifier.padding(8.dp).size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                letterSpacing = 1.5.sp,
                color = Color.White
            )
        }
        content()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MuscleGroupPremium(label: String, muscles: List<String>, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = label, 
            fontSize = 11.sp, 
            fontWeight = FontWeight.Black, 
            color = Color.Gray,
            letterSpacing = 1.sp
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            muscles.forEach { muscle ->
                Surface(
                    color = color.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = muscle.uppercase(),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
            }
        }
    }
}
