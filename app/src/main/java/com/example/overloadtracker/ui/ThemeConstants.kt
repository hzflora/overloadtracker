package com.example.overloadtracker.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object PremiumTheme {
    // Primary Brand Colors
    val BrandRed = Color(0xFFE53935)
    val IronOrange = Color(0xFFFF5722)
    val DeepSlate = Color(0xFF0A0A0A) // Even darker for more premium feel
    val Charcoal = Color(0xFF161616)
    val SurfaceGray = Color(0xFF222222)
    
    // Accent Colors
    val ElectricBlue = Color(0xFF2196F3)
    val SuccessGreen = Color(0xFF4CAF50)
    
    // Gradients
    val BrandGradient = androidx.compose.ui.graphics.Brush.verticalGradient(
        colors = listOf(BrandRed, Color(0xFFB71C1C))
    )
    
    // Spacing & Shapes
    val CornerLarge = 32.dp
    val CornerMedium = 16.dp
    val PaddingStandard = 24.dp
}