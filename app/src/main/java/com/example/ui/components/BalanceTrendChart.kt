package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CurrencyConfig
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.util.CurrencyFormatter

@Composable
fun BalanceTrendChart(
    points: List<Pair<String, Double>>,
    currency: CurrencyConfig,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return

    val progress = remember { Animatable(0f) }

    LaunchedEffect(points) {
        progress.snapTo(0f)
        progress.animateTo(1f, animationSpec = tween(600))
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Balance Trend",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            val currentBal = points.lastOrNull()?.second ?: 0.0
            Text(
                text = "Net worth progression: ${CurrencyFormatter.format(currentBal, currency)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            val minVal = points.minOfOrNull { it.second } ?: 0.0
            val maxVal = points.maxOfOrNull { it.second } ?: 1000.0
            val range = (maxVal - minVal).coerceAtLeast(100.0)
            val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height - 20.dp.toPx()
                    val count = points.size
                    val stepX = if (count > 1) w / (count - 1) else w

                    // Guide lines
                    for (i in 0..2) {
                        val y = h * (i / 2f)
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    val linePath = Path()
                    val fillPath = Path()
                    val drawPoints = mutableListOf<Offset>()

                    points.forEachIndexed { i, pair ->
                        val x = i * stepX
                        val normalized = ((pair.second - minVal) / range).toFloat().coerceIn(0f, 1f) * progress.value
                        val y = h - (normalized * h)
                        val off = Offset(x, y)
                        drawPoints.add(off)

                        if (i == 0) {
                            linePath.moveTo(x, y)
                            fillPath.moveTo(x, h)
                            fillPath.lineTo(x, y)
                        } else {
                            val prev = drawPoints[i - 1]
                            val cx = (prev.x + x) / 2f
                            linePath.cubicTo(cx, prev.y, cx, y, x, y)
                            fillPath.cubicTo(cx, prev.y, cx, y, x, y)
                        }

                        if (i == count - 1) {
                            fillPath.lineTo(x, h)
                            fillPath.close()
                        }
                    }

                    // Fill
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(EmeraldLight.copy(alpha = 0.3f), Color.Transparent),
                            startY = 0f,
                            endY = h
                        )
                    )

                    // Stroke
                    drawPath(
                        path = linePath,
                        color = EmeraldPrimary,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Circles
                    drawPoints.forEach { off ->
                        drawCircle(color = Color.White, radius = 4.5.dp.toPx(), center = off)
                        drawCircle(color = EmeraldPrimary, radius = 3.dp.toPx(), center = off)
                    }
                }
            }

            // Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                points.forEach { pair ->
                    Text(
                        text = pair.first,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
