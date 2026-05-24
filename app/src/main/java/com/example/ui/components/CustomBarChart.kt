package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TealAccent
import com.example.ui.theme.TealPrimary

data class BarChartItem(
    val label: String,
    val value: Float,
    val color: Color = TealPrimary
)

@Composable
fun CustomBarChart(
    items: List<BarChartItem>,
    modifier: Modifier = Modifier,
    height: Int = 220,
    currencyPrefix: String = "",
    textColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
    gridLineColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
) {
    if (items.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(height.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "No chart records available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
        return
    }

    val maxVal = remember(items) { items.maxOf { it.value }.coerceAtLeast(1f) }
    var scaleTriggered by remember { mutableStateOf(false) }
    
    // Scale animation
    LaunchedEffect(items) {
        scaleTriggered = true
    }

    val scaleFactor by animateFloatAsState(
        targetValue = if (scaleTriggered) 1.0f else 0.0f,
        animationSpec = tween(durationMillis = 800),
        label = "BarScale"
    )

    val gridColor = gridLineColor
    val labelColor = textColor.toArgb()

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height.dp)
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val spacing = 24f
            val barCount = items.size
            val individualBarWidth = (canvasWidth - spacing * (barCount + 1)) / barCount

            // 1. Draw horizontal horizontal gridlines (3 grid levels)
            val gridLinesCount = 3
            for (i in 0..gridLinesCount) {
                val gridY = canvasHeight * i / gridLinesCount
                drawLine(
                    color = gridColor,
                    start = Offset(0f, gridY),
                    end = Offset(canvasWidth, gridY),
                    strokeWidth = 1f
                )
                // Draw labels on the y axis
                val yVal = maxVal * (gridLinesCount - i) / gridLinesCount
                val formattedY = if (currencyPrefix.isNotEmpty()) {
                    "$currencyPrefix${yVal.toInt()}"
                } else {
                    yVal.toInt().toString()
                }
                drawContext.canvas.nativeCanvas.drawText(
                    formattedY,
                    10f,
                    gridY - 8f,
                    android.graphics.Paint().apply {
                        color = labelColor
                        textSize = 28f
                        isAntiAlias = true
                    }
                )
            }

            // 2. Draw each bar
            for (idx in items.indices) {
                val item = items[idx]
                val currentBarHeight = (item.value / maxVal) * canvasHeight * scaleFactor

                val barLeft = spacing + (individualBarWidth + spacing) * idx
                val barTop = canvasHeight - currentBarHeight

                // Gradient brush for bar filling
                val gradientBrush = Brush.verticalGradient(
                    colors = listOf(
                        GoldPrimary,
                        item.color,
                        item.color.copy(alpha = 0.4f)
                    )
                )

                // Paint the rounded rectangles bar
                drawRoundRect(
                    brush = gradientBrush,
                    topLeft = Offset(barLeft, barTop),
                    size = Size(individualBarWidth, currentBarHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
                )

                // Optional label / value display on top of bar
                val labelText = if (currencyPrefix.isNotEmpty()) {
                    "$currencyPrefix${item.value.toInt()}"
                } else {
                    item.value.toInt().toString()
                }

                if (currentBarHeight > 30f) {
                    drawContext.canvas.nativeCanvas.drawText(
                        labelText,
                        barLeft + individualBarWidth / 2f,
                        (barTop + 40f).coerceAtMost(canvasHeight - 15f),
                        android.graphics.Paint().apply {
                            color = Color.White.toArgb()
                            textSize = 30f
                            textAlign = android.graphics.Paint.Align.CENTER
                            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                            isAntiAlias = true
                        }
                    )
                }

                // Label on X-axis representing the category
                drawContext.canvas.nativeCanvas.drawText(
                    item.label,
                    barLeft + individualBarWidth / 2f,
                    canvasHeight + 40f,
                    android.graphics.Paint().apply {
                        color = labelColor
                        textSize = 32f
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
    }
}
