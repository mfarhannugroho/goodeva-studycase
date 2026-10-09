package com.goodeva.bletracker.presentation.radar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun RssiSparkline(
    history: List<Int>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    if (history.size < 2) {
        Box(modifier = modifier.fillMaxWidth().height(60.dp))
        return
    }

    val minVal = -95f
    val maxVal = -20f

    Canvas(modifier = modifier.fillMaxWidth().height(60.dp)) {
        val width = size.width
        val height = size.height

        val stepX = width / (history.size - 1).coerceAtLeast(1)

        val points = history.mapIndexed { index, rssi ->
            val normalizedY = 1f - ((rssi.toFloat() - minVal) / (maxVal - minVal)).coerceIn(0f, 1f)
            Offset(x = index * stepX, y = normalizedY * height)
        }

        val strokePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
        }

        val fillPath = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
            lineTo(points.last().x, height)
            lineTo(points.first().x, height)
            close()
        }

        // 1. Shaded area gradient
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    lineColor.copy(alpha = 0.35f),
                    Color.Transparent
                ),
                startY = 0f,
                endY = height
            )
        )

        // 2. Smooth line stroke
        drawPath(
            path = strokePath,
            color = lineColor,
            style = Stroke(
                width = 3.5f,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // 3. Dot on the most recent point
        val latest = points.last()
        drawCircle(
            color = lineColor,
            radius = 5f,
            center = latest
        )
        drawCircle(
            color = Color.White,
            radius = 2.5f,
            center = latest
        )
    }
}
