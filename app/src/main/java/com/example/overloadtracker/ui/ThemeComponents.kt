package com.example.overloadtracker.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PremiumTextField(value: String, onValueChange: (String) -> Unit, label: String, brandColor: Color) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = brandColor,
            unfocusedBorderColor = Color.Gray,
            focusedLabelColor = brandColor,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        )
    )
}

@Composable
fun PremiumDialog(
    onDismiss: () -> Unit,
    title: String,
    confirmLabel: String,
    brandColor: Color,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PremiumTheme.Charcoal,
        title = { Text(title, fontWeight = FontWeight.Black, letterSpacing = 1.sp, color = Color.White) },
        text = { content() },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = brandColor, contentColor = Color.White),
                shape = RoundedCornerShape(8.dp)
            ) { Text(confirmLabel, fontWeight = FontWeight.Black) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)) {
                Text("İPTAL")
            }
        }
    )
}
