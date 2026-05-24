package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GoldPrimary

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    borderColor: Color = GoldPrimary.copy(alpha = 0.5f),
    backgroundBrush: Brush? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == com.example.ui.theme.BgDeepDark
    
    // Choose gradient brush based on theme
    val glassBrush = backgroundBrush ?: if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                com.example.ui.theme.SurfaceDark.copy(alpha = 0.85f),
                com.example.ui.theme.BgDeepDark.copy(alpha = 0.95f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.95f),
                com.example.ui.theme.BgLight.copy(alpha = 0.90f)
            )
        )
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                clip = false,
                ambientColor = com.example.ui.theme.TealSecondary.copy(alpha = 0.15f),
                spotColor = GoldPrimary.copy(alpha = 0.12f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(glassBrush)
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        borderColor,
                        borderColor.copy(alpha = 0.15f),
                        borderColor
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        content = content
    )
}
