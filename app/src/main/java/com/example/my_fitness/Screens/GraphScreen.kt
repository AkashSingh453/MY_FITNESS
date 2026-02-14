package com.example.my_fitness.Screens

import InitAppScaler
import android.annotation.SuppressLint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pDp
import pSp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphScreen(
    dataPoints: List<GraphData>,
    targetWeight: Float = 70f
) {
    InitAppScaler()
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (dataPoints.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No records yet.", color = Color.Gray, fontSize = 16.pSp)
            }
        } else {
            ScrollableWeightGraph(
                data = dataPoints,
                targetValue = targetWeight,
                lineColor = Color(0xFF1565C0)
            )
        }
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ScrollableWeightGraph(data: List<GraphData>, targetValue: Float, lineColor: Color) {
    val pointSpacing = 70.pDp

    // Ensure range covers the target weight
    val maxValue = (data.maxOf { it.value }.coerceAtLeast(targetValue)) * 1.05f
    val minValue = (data.minOf { it.value }.coerceAtMost(targetValue)) * 0.95f
    val range = maxValue - minValue

    val markingCount = 8
    val weightSteps = remember(maxValue, minValue) {
        List(markingCount) { i -> minValue + (range * (i / (markingCount - 1).toFloat())) }.reversed()
    }

    val scrollState = rememberScrollState()
    LaunchedEffect(data) { scrollState.animateScrollTo(scrollState.maxValue) }

    // Padding values used in Canvas (Needed for calculating Goal Text Position)
    val graphTopPadding = 20.pDp
    val graphBottomPadding = 60.pDp

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        val containerHeight = maxHeight
        val totalWidth = (data.size * pointSpacing.value).dp.coerceAtLeast(maxWidth)
        val goalColor = Color(0xFF4CAF50)

        // 1. SCROLLABLE GRAPH CONTENT
        Box(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(scrollState)
        ) {
            Canvas(
                modifier = Modifier
                    .width(totalWidth)
                    .fillMaxHeight()
                    .padding(top = graphTopPadding, bottom = graphBottomPadding)
            ) {
                val heightPx = size.height

                // --- Grid Lines ---
                weightSteps.forEach { weight ->
                    val y = (1f - ((weight - minValue) / range)) * heightPx
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.2f),
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.pDp.toPx()
                    )
                }

                // --- Dotted Goal Line ---
                val targetY = (1f - ((targetValue - minValue) / range)) * heightPx
                drawLine(
                    color = goalColor.copy(alpha = 0.8f),
                    start = Offset(x = 0f, y = targetY),
                    end = Offset(x = size.width, y = targetY),
                    strokeWidth = 2.pDp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f),
                    cap = StrokeCap.Round
                )

                // --- Data Path ---
                val widthPerPoint = pointSpacing.toPx()
                val points = data.mapIndexed { index, item ->
                    Offset(
                        x = (index * widthPerPoint) + (widthPerPoint / 2),
                        y = (1f - ((item.value - minValue) / range)) * heightPx
                    )
                }

                val path = Path().apply {
                    if (points.isNotEmpty()) {
                        moveTo(points.first().x, points.first().y)
                        for (i in 0 until points.size - 1) {
                            val p1 = points[i]
                            val p2 = points[i + 1]
                            val cp1 = Offset(p1.x + (p2.x - p1.x) / 2f, p1.y)
                            val cp2 = Offset(p1.x + (p2.x - p1.x) / 2f, p2.y)
                            cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, p2.x, p2.y)
                        }
                    }
                }

                drawPath(path = path, color = lineColor, style = Stroke(width = 4.pDp.toPx(), cap = StrokeCap.Round))

                points.forEach {
                    drawCircle(color = Color.White, radius = 5.pDp.toPx(), center = it)
                    drawCircle(color = lineColor, radius = 3.pDp.toPx(), center = it)
                }
            }

            // --- Timestamps ---
            Row(
                modifier = Modifier
                    .width(totalWidth)
                    .align(Alignment.BottomCenter)
                    .height(graphBottomPadding)
            ) {
                data.forEach { item ->
                    Box(modifier = Modifier.width(pointSpacing).fillMaxHeight(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "${item.value}", fontSize = 10.pSp, fontWeight = FontWeight.Bold, color = lineColor)
                            Text(text = formatDate(item.timestamp), fontSize = 9.pSp, color = Color.Gray)
                        }
                    }
                }
            }
        }

        // 2. RIGHT SIDE Y-AXIS LABELS (Pinned)
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .align(Alignment.CenterEnd)
                .padding(top = graphTopPadding, bottom = graphBottomPadding),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
        ) {
            weightSteps.forEach { weight ->
                Text(
                    text = "${weight.toInt()}",
                    fontSize = 9.pSp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray.copy(alpha = 0.6f)
                )
            }
        }

        // 3. 🔥 LEFT SIDE GOAL LABEL (Pinned)
        // We calculate the Y position manually so it sits exactly on the dotted line
        // but stays pinned to the left while the graph scrolls.
        val drawingHeight = containerHeight - graphTopPadding - graphBottomPadding
        val fraction = (targetValue - minValue) / range

        // Calculate Offset: TopPadding + (Height * inverted_fraction) - HalfTextHeight
        val labelYOffset = graphTopPadding + (drawingHeight * (1f - fraction)) - 10.pDp

        Box(
            modifier = Modifier
                .offset(x = 8.pDp, y = labelYOffset)
                .background(goalColor, RoundedCornerShape(4.pDp))
                .padding(horizontal = 1.pDp, vertical = 0.pDp)
        ) {
            Text(
                text = "Goal: ${targetValue.toInt()}",
                color = Color.White,
                fontSize = 9.pSp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// Data Model and Date Formatting
data class GraphData(val value: Float, val timestamp: Long)

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
    return sdf.format(Date(timestamp))
}