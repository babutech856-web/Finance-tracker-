package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CurrencyConfig
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.viewmodel.CashFlowPoint
import com.example.util.CurrencyFormatter
import kotlin.math.max

@Composable
fun CashFlowChart(
    points: List<CashFlowPoint>,
    currency: CurrencyConfig,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) {
        Card(
            modifier = modifier.fillMaxWidth().height(220.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "No financial activity for this period",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(points) {
        selectedIndex = null
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
            // Header & Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Income vs Expense",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(IncomeGreen, CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Income",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.size(8.dp).background(ExpenseRed, CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Expense",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Interactive Tooltip when point tapped
            if (selectedIndex != null && selectedIndex!! in points.indices) {
                val pt = points[selectedIndex!!]
                Surface(
                    modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = pt.fullDateLabel,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "In: ${CurrencyFormatter.format(pt.income, currency)}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = IncomeGreen
                            )
                            Text(
                                text = "Out: ${CurrencyFormatter.format(pt.expense, currency)}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = ExpenseRed
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "Tap a point to view exact figures",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Canvas Chart
            val maxVal = points.maxOfOrNull { max(it.income, it.expense) }?.coerceAtLeast(1000.0) ?: 1000.0
            val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
            val indicatorColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(points) {
                            detectTapGestures { offset ->
                                val spacing = size.width / (points.size.coerceAtLeast(2) - 1).toFloat()
                                val nearestIndex = (offset.x / spacing).toInt().coerceIn(0, points.size - 1)
                                selectedIndex = if (selectedIndex == nearestIndex) null else nearestIndex
                            }
                        }
                ) {
                    val w = size.width
                    val h = size.height - 24.dp.toPx() // leave room for labels
                    val count = points.size
                    val stepX = if (count > 1) w / (count - 1) else w

                    // Draw 3 horizontal guide lines
                    for (i in 0..2) {
                        val y = h * (i / 2f)
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    val anim = progress.value

                    // Income Path
                    val incomePath = Path()
                    val incomeFillPath = Path()
                    // Expense Path
                    val expensePath = Path()
                    val expenseFillPath = Path()

                    val incomePoints = mutableListOf<Offset>()
                    val expensePoints = mutableListOf<Offset>()

                    points.forEachIndexed { index, pt ->
                        val x = index * stepX
                        val normalizedInc = (pt.income / maxVal).toFloat().coerceIn(0f, 1f) * anim
                        val normalizedExp = (pt.expense / maxVal).toFloat().coerceIn(0f, 1f) * anim

                        val yInc = h - (normalizedInc * h)
                        val yExp = h - (normalizedExp * h)

                        val offInc = Offset(x, yInc)
                        val offExp = Offset(x, yExp)
                        incomePoints.add(offInc)
                        expensePoints.add(offExp)

                        if (index == 0) {
                            incomePath.moveTo(x, yInc)
                            incomeFillPath.moveTo(x, h)
                            incomeFillPath.lineTo(x, yInc)

                            expensePath.moveTo(x, yExp)
                            expenseFillPath.moveTo(x, h)
                            expenseFillPath.lineTo(x, yExp)
                        } else {
                            val prevInc = incomePoints[index - 1]
                            val cx = (prevInc.x + x) / 2f
                            incomePath.cubicTo(cx, prevInc.y, cx, yInc, x, yInc)
                            incomeFillPath.cubicTo(cx, prevInc.y, cx, yInc, x, yInc)

                            val prevExp = expensePoints[index - 1]
                            val cxExp = (prevExp.x + x) / 2f
                            expensePath.cubicTo(cxExp, prevExp.y, cxExp, yExp, x, yExp)
                            expenseFillPath.cubicTo(cxExp, prevExp.y, cxExp, yExp, x, yExp)
                        }

                        if (index == count - 1) {
                            incomeFillPath.lineTo(x, h)
                            incomeFillPath.close()

                            expenseFillPath.lineTo(x, h)
                            expenseFillPath.close()
                        }
                    }

                    // Draw Gradient Fills
                    drawPath(
                        path = incomeFillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(IncomeGreen.copy(alpha = 0.25f), Color.Transparent),
                            startY = 0f,
                            endY = h
                        )
                    )
                    drawPath(
                        path = expenseFillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(ExpenseRed.copy(alpha = 0.15f), Color.Transparent),
                            startY = 0f,
                            endY = h
                        )
                    )

                    // Draw Stroke Lines
                    drawPath(
                        path = incomePath,
                        color = IncomeGreen,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawPath(
                        path = expensePath,
                        color = ExpenseRed,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw Circles on Points
                    incomePoints.forEachIndexed { index, off ->
                        val isSelected = selectedIndex == index
                        drawCircle(
                            color = Color.White,
                            radius = if (isSelected) 6.dp.toPx() else 4.dp.toPx(),
                            center = off
                        )
                        drawCircle(
                            color = IncomeGreen,
                            radius = if (isSelected) 4.5.dp.toPx() else 2.5.dp.toPx(),
                            center = off
                        )
                    }

                    expensePoints.forEachIndexed { index, off ->
                        val isSelected = selectedIndex == index
                        drawCircle(
                            color = Color.White,
                            radius = if (isSelected) 6.dp.toPx() else 4.dp.toPx(),
                            center = off
                        )
                        drawCircle(
                            color = ExpenseRed,
                            radius = if (isSelected) 4.5.dp.toPx() else 2.5.dp.toPx(),
                            center = off
                        )
                    }

                    // Draw Vertical Indicator for Selected Index
                    if (selectedIndex != null && selectedIndex!! in points.indices) {
                        val selX = selectedIndex!! * stepX
                        drawLine(
                            color = indicatorColor,
                            start = Offset(selX, 0f),
                            end = Offset(selX, h),
                            strokeWidth = 1.5.dp.toPx()
                        )
                    }
                }
            }

            // Bottom X-Axis Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                points.forEachIndexed { index, pt ->
                    val isSelected = selectedIndex == index
                    Text(
                        text = pt.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
