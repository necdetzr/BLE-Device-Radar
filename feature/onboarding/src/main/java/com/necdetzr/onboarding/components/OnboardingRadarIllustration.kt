package com.necdetzr.onboarding.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp

@Composable
internal fun OnboardingRadarIllustration(
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(
        label = "onboardingRadar",
    )
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = FULL_ROTATION,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = RADAR_DURATION,
                easing = LinearEasing,
            ),
        ),
        label = "radarRotation",
    )
    val pulse by transition.animateFloat(
        initialValue = MINIMUM_PULSE,
        targetValue = MAXIMUM_PULSE,
        animationSpec = infiniteRepeatable(
            animation = tween(PULSE_DURATION),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "devicePulse",
    )

    RadarCanvas(
        rotation = rotation,
        pulse = pulse,
        modifier = modifier,
    )
}

@Composable
private fun RadarCanvas(
    rotation: Float,
    pulse: Float,
    modifier: Modifier = Modifier,
) {
    val primary = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant

    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 360.dp),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            tonalElevation = 2.dp,
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
            ) {
                val center = Offset(
                    x = size.width / 2f,
                    y = size.height / 2f,
                )
                val radius = size.minDimension / 2f

                drawRadarGrid(
                    center = center,
                    radius = radius,
                    color = gridColor,
                )
                drawRadarBeam(
                    center = center,
                    radius = radius,
                    rotation = rotation,
                    color = primary,
                )
                drawRadarDevices(
                    color = primary,
                    pulse = pulse,
                )
                drawCircle(
                    color = primary,
                    radius = 5.dp.toPx(),
                    center = center,
                )
            }
        }
    }
}

private fun DrawScope.drawRadarGrid(
    center: Offset,
    radius: Float,
    color: Color,
) {
    listOf(
        radius,
        radius * MIDDLE_CIRCLE_SCALE,
        radius * INNER_CIRCLE_SCALE,
    ).forEach { circleRadius ->
        drawCircle(
            color = color,
            radius = circleRadius,
            center = center,
            style = Stroke(width = 1.dp.toPx()),
        )
    }
}

private fun DrawScope.drawRadarBeam(
    center: Offset,
    radius: Float,
    rotation: Float,
    color: Color,
) {
    rotate(
        degrees = rotation,
        pivot = center,
    ) {
        drawArc(
            brush = Brush.sweepGradient(
                colors = listOf(
                    Color.Transparent,
                    color.copy(alpha = 0.12f),
                    color.copy(alpha = 0.65f),
                ),
                center = center,
            ),
            startAngle = -BEAM_ANGLE,
            sweepAngle = BEAM_ANGLE,
            useCenter = true,
            topLeft = Offset(
                x = center.x - radius,
                y = center.y - radius,
            ),
            size = Size(
                width = radius * 2f,
                height = radius * 2f,
            ),
        )
    }
}

private fun DrawScope.drawRadarDevices(
    color: Color,
    pulse: Float,
) {
    val positions = listOf(
        Offset(size.width * 0.72f, size.height * 0.30f),
        Offset(size.width * 0.27f, size.height * 0.56f),
        Offset(size.width * 0.70f, size.height * 0.75f),
    )

    positions.forEachIndexed { index, position ->
        val radius = (DEVICE_DOT_RADIUS - index).dp.toPx()

        drawCircle(
            color = color.copy(alpha = 0.18f),
            radius = radius * pulse * DEVICE_GLOW_SCALE,
            center = position,
        )
        drawCircle(
            color = color,
            radius = radius,
            center = position,
        )
    }
}

private const val FULL_ROTATION = 360f
private const val RADAR_DURATION = 3_000
private const val PULSE_DURATION = 1_000
private const val MINIMUM_PULSE = 0.75f
private const val MAXIMUM_PULSE = 1.15f
private const val MIDDLE_CIRCLE_SCALE = 0.66f
private const val INNER_CIRCLE_SCALE = 0.33f
private const val BEAM_ANGLE = 58f
private const val DEVICE_DOT_RADIUS = 6
private const val DEVICE_GLOW_SCALE = 2.4f
