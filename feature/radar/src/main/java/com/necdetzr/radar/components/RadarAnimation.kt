package com.necdetzr.radar.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun RadarAnimation(
    isScanning: Boolean,
    radarSize: Dp = DEFAULT_RADAR_SIZE_DP.dp,
) {
    val rotation = remember {
        Animatable(0f)
    }
    LaunchedEffect(isScanning) {
        if (isScanning) {
            rotation.snapTo(0f)

            while (true) {
                rotation.animateTo(
                    targetValue = rotation.value + FULL_ROTATION,
                    animationSpec = tween(
                        durationMillis = ROTATION_DURATION_MILLIS,
                        easing = LinearEasing,
                    ),
                )
            }
        } else {
            rotation.stop()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(radarSize)
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier.size(radarSize),
        ) {
            drawRadar(
                isScanning = isScanning,
                rotation = rotation.value,
            )
        }
    }
}
private fun DrawScope.drawRadar(
    isScanning: Boolean,
    rotation: Float,
) {
    val center = Offset(
        x = size.width / 2,
        y = size.height / 2,
    )
    val maxRadius = size.minDimension / 2

    drawRadarCircles(
        center = center,
        maxRadius = maxRadius,
    )

    if (isScanning) {
        drawRadarSweep(
            center = center,
            maxRadius = maxRadius,
            rotation = rotation,
        )
    }

    drawCircle(
        color = Color.Cyan,
        radius = 4.dp.toPx(),
        center = center,
    )
}
private fun DrawScope.drawRadarCircles(
    center: Offset,
    maxRadius: Float,
) {
    val stroke = Stroke(width = 1.dp.toPx())
    val circleColor = Color(0xFF334155)

    drawCircle(
        color = circleColor,
        radius = maxRadius,
        center = center,
        style = stroke,
    )
    drawCircle(
        color = circleColor,
        radius = maxRadius * 0.66f,
        center = center,
        style = stroke,
    )
    drawCircle(
        color = circleColor,
        radius = maxRadius * 0.33f,
        center = center,
        style = stroke,
    )
}
private fun DrawScope.drawRadarSweep(
    center: Offset,
    maxRadius: Float,
    rotation: Float,
) {
    val sweepGradient = Brush.sweepGradient(
        colors = listOf(
            Color.Transparent,
            Color.Transparent,
            Color.Transparent,
            Color.Transparent,
            Color(0xFF38BDF8).copy(alpha = 0.1f),
            Color(0xFF38BDF8).copy(alpha = 0.5f),
        ),
        center = center,
    )

    withTransform(
        transformBlock = {
            rotate(
                degrees = rotation,
                pivot = center,
            )
        },
    ) {
        drawArc(
            brush = sweepGradient,
            startAngle = 0f,
            sweepAngle = FULL_ROTATION,
            useCenter = true,
            topLeft = Offset(
                x = center.x - maxRadius,
                y = center.y - maxRadius,
            ),
            size = Size(
                width = maxRadius * 2,
                height = maxRadius * 2,
            ),
        )
    }
}
private const val FULL_ROTATION = 360f
private const val ROTATION_DURATION_MILLIS = 2_500
private const val DEFAULT_RADAR_SIZE_DP = 200
