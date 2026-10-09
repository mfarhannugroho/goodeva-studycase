package com.goodeva.bletracker.presentation.radar

import android.graphics.Paint
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.goodeva.bletracker.domain.model.ProximityZone
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RadarCanvas(
    smoothedRssi: Double,
    proximityZone: ProximityZone,
    isSignalLost: Boolean,
    modifier: Modifier = Modifier
) {
    // 1. Animasi putaran jarum radar sweep 360 derajat
    val infiniteTransition = rememberInfiniteTransition(label = "RadarSweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SweepAngle"
    )

    // 2. Animasi denyut / pulse pada target blip
    val blipPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "BlipPulse"
    )

    // 3. Normalisasi jarak radial [0.18 s/d 0.95] berdasarkan RSSI
    val targetFraction = when {
        isSignalLost -> 0.98f
        smoothedRssi >= -30.0 -> 0.18f // < 1m
        smoothedRssi >= -50.0 -> 0.38f // 1-3m
        smoothedRssi >= -70.0 -> 0.60f // 3-10m
        smoothedRssi >= -80.0 -> 0.78f // 10-20m
        else -> 0.92f // > 20m
    }

    val animatedRadialFraction by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "RadialDistance"
    )

    val blipColor = if (isSignalLost) Color(0xFF757575) else proximityZone.color

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = (minOf(size.width, size.height) / 2f) * 0.90f

            // Background Radial Glow untuk kedalaman radar
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0F1E38),
                        Color(0xFF091021),
                        Color(0xFF070B16)
                    ),
                    center = center,
                    radius = maxRadius
                ),
                radius = maxRadius,
                center = center
            )

            // Rasio cincin konsentris untuk 5 zona jarak
            val ringFractions = listOf(0.20f, 0.40f, 0.62f, 0.82f, 1.0f)
            val ringLabels = listOf("< 1m", "1-3m", "3-10m", "10-20m", "> 20m")

            // A. Lingkaran konsentris zona jarak & label teks
            ringFractions.forEachIndexed { index, fraction ->
                val radius = maxRadius * fraction
                val strokeColor = if (index == ringFractions.lastIndex) {
                    Color(0xFF00F5FF).copy(alpha = 0.50f)
                } else {
                    Color(0xFF00F5FF).copy(alpha = 0.18f)
                }

                drawCircle(
                    color = strokeColor,
                    radius = radius,
                    center = center,
                    style = Stroke(
                        width = if (index == ringFractions.lastIndex) 2.5f else 1.2f,
                        pathEffect = if (index % 2 == 1) PathEffect.dashPathEffect(floatArrayOf(12f, 8f)) else null
                    )
                )

                // Label jarak pada sumbu vertikal atas
                drawIntoCanvas { canvas ->
                    val paint = Paint().apply {
                        color = Color(0xFF64748B).toArgb()
                        textSize = 22f
                        isAntiAlias = true
                        textAlign = Paint.Align.CENTER
                    }
                    canvas.nativeCanvas.drawText(
                        ringLabels[index],
                        center.x,
                        center.y - radius + 24f,
                        paint
                    )
                }
            }

            // B. Garis bidik silang (Crosshairs)
            val crosshairColor = Color(0xFF00F5FF).copy(alpha = 0.18f)
            drawLine(
                color = crosshairColor,
                start = Offset(center.x - maxRadius, center.y),
                end = Offset(center.x + maxRadius, center.y),
                strokeWidth = 1f
            )
            drawLine(
                color = crosshairColor,
                start = Offset(center.x, center.y - maxRadius),
                end = Offset(center.x, center.y + maxRadius),
                strokeWidth = 1f
            )

            // C. Jarum Pemindai & Efek Sapuan Radar Berputar (Sweeping Line & Arc)
            rotate(sweepAngle, pivot = center) {
                // Efek phosphor trail
                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to Color.Transparent,
                        0.88f to Color.Transparent,
                        0.97f to Color(0x3300F5FF),
                        1.0f to Color(0x7700F5FF),
                        center = center
                    ),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = true,
                    topLeft = Offset(center.x - maxRadius, center.y - maxRadius),
                    size = androidx.compose.ui.geometry.Size(maxRadius * 2f, maxRadius * 2f)
                )

                // Garis sapuan depan yang tajam
                drawLine(
                    color = Color(0xFF00F5FF),
                    start = center,
                    end = Offset(center.x + maxRadius, center.y),
                    strokeWidth = 2.5f,
                    cap = StrokeCap.Round
                )
            }

            // D. Titik Target Perangkat (Pulsing Blip)
            val blipAngleRad = Math.toRadians(45.0)
            val blipDistance = maxRadius * animatedRadialFraction
            val blipCenter = Offset(
                x = (center.x + blipDistance * cos(blipAngleRad)).toFloat(),
                y = (center.y - blipDistance * sin(blipAngleRad)).toFloat()
            )

            if (!isSignalLost) {
                // Multi-tier ripple glow
                drawCircle(
                    color = blipColor.copy(alpha = (1f - (blipPulse - 1f) / 1.4f).coerceIn(0f, 0.45f)),
                    radius = 16f * blipPulse,
                    center = blipCenter
                )
                drawCircle(
                    color = blipColor.copy(alpha = (1f - (blipPulse - 1f) / 1.8f).coerceIn(0f, 0.25f)),
                    radius = 24f * blipPulse,
                    center = blipCenter
                )
            }

            // Titik inti blip
            drawCircle(
                color = blipColor,
                radius = 11f,
                center = blipCenter
            )
            drawCircle(
                color = Color.White,
                radius = 4.5f,
                center = blipCenter
            )

            // E. Titik Pusat Pengguna (Origin / "YOU")
            drawCircle(
                color = Color(0xFF00F5FF),
                radius = 6f,
                center = center
            )
            drawCircle(
                color = Color(0xFF00F5FF).copy(alpha = 0.35f),
                radius = 13f,
                center = center,
                style = Stroke(width = 1.8f)
            )
        }
    }
}
