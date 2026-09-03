package com.igames.kids.games.trafficlight.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.igames.kids.core.theme.LampGreenOff
import com.igames.kids.core.theme.LampGreenOn
import com.igames.kids.core.theme.LampRedOff
import com.igames.kids.core.theme.LampRedOn
import com.igames.kids.core.theme.LampVisorColor
import com.igames.kids.games.trafficlight.model.TrafficLightState

@Composable
fun PedestrianLightView(
    state: TrafficLightState,
    isBlinkVisible: Boolean,
    lampSize: Dp = 120.dp,
    isHorizontal: Boolean = false,
    onLampClick: ((TrafficLightState) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isRedActive = state.isRed || state.isYellow
    val isGreenActive = if (state == TrafficLightState.GREEN_BLINK) isBlinkVisible else state.isGreen

    // Walking animation phase for green pedestrian
    val infiniteTransition = rememberInfiniteTransition(label = "pedestrian_walk")
    val walkStepPhase by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "walkStepPhase"
    )

    if (isHorizontal) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PedestrianLamp(
                isRed = true,
                isOn = isRedActive,
                size = lampSize,
                walkPhase = 0f,
                onClick = { onLampClick?.invoke(TrafficLightState.RED) }
            )
            PedestrianLamp(
                isRed = false,
                isOn = isGreenActive,
                size = lampSize,
                walkPhase = if (isGreenActive) walkStepPhase else 0f,
                onClick = { onLampClick?.invoke(TrafficLightState.GREEN) }
            )
        }
    } else {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PedestrianLamp(
                isRed = true,
                isOn = isRedActive,
                size = lampSize,
                walkPhase = 0f,
                onClick = { onLampClick?.invoke(TrafficLightState.RED) }
            )
            PedestrianLamp(
                isRed = false,
                isOn = isGreenActive,
                size = lampSize,
                walkPhase = if (isGreenActive) walkStepPhase else 0f,
                onClick = { onLampClick?.invoke(TrafficLightState.GREEN) }
            )
        }
    }
}

@Composable
fun PedestrianLamp(
    isRed: Boolean,
    isOn: Boolean,
    size: Dp,
    walkPhase: Float,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(size)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = onClick != null,
                onClick = { onClick?.invoke() }
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = this.size.minDimension / 2.2f

            // Visor
            val visorPath = Path().apply {
                arcTo(
                    rect = androidx.compose.ui.geometry.Rect(
                        left = center.x - radius * 1.25f,
                        top = center.y - radius * 1.25f,
                        right = center.x + radius * 1.25f,
                        bottom = center.y + radius * 1.25f
                    ),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = 180f,
                    forceMoveTo = true
                )
            }
            drawPath(
                path = visorPath,
                color = LampVisorColor,
                style = Stroke(width = radius * 0.22f)
            )

            // Lamp dark background
            drawCircle(
                color = Color(0xFF101316),
                radius = radius * 1.05f,
                center = center
            )

            val baseColor = if (isRed) {
                if (isOn) LampRedOn else LampRedOff
            } else {
                if (isOn) LampGreenOn else LampGreenOff
            }

            // Glow bloom when ON
            if (isOn) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            baseColor.copy(alpha = 0.45f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = radius * 1.4f
                    ),
                    radius = radius * 1.4f,
                    center = center
                )
            }

            // Lamp Lens surface
            drawCircle(
                color = if (isOn) Color(0xFF1B2329) else Color(0xFF101214),
                radius = radius,
                center = center
            )

            // Draw Figure
            val figureColor = if (isOn) baseColor else baseColor.copy(alpha = 0.35f)
            if (isRed) {
                drawStandingFigure(center, radius, figureColor)
            } else {
                drawWalkingFigure(center, radius, figureColor, walkPhase)
            }
        }
    }
}

private fun DrawScope.drawStandingFigure(center: Offset, radius: Float, color: Color) {
    val headRadius = radius * 0.16f
    val headCenter = Offset(center.x, center.y - radius * 0.45f)
    // Head
    drawCircle(color = color, radius = headRadius, center = headCenter)

    // Body
    val bodyPath = Path().apply {
        moveTo(center.x - radius * 0.22f, center.y - radius * 0.22f)
        lineTo(center.x + radius * 0.22f, center.y - radius * 0.22f)
        lineTo(center.x + radius * 0.18f, center.y + radius * 0.15f)
        lineTo(center.x - radius * 0.18f, center.y + radius * 0.15f)
        close()
    }
    drawPath(path = bodyPath, color = color)

    // Arms down
    drawLine(
        color = color,
        start = Offset(center.x - radius * 0.22f, center.y - radius * 0.2f),
        end = Offset(center.x - radius * 0.26f, center.y + radius * 0.12f),
        strokeWidth = radius * 0.11f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = color,
        start = Offset(center.x + radius * 0.22f, center.y - radius * 0.2f),
        end = Offset(center.x + radius * 0.26f, center.y + radius * 0.12f),
        strokeWidth = radius * 0.11f,
        cap = StrokeCap.Round
    )

    // Legs straight standing together
    drawLine(
        color = color,
        start = Offset(center.x - radius * 0.10f, center.y + radius * 0.15f),
        end = Offset(center.x - radius * 0.10f, center.y + radius * 0.62f),
        strokeWidth = radius * 0.13f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = color,
        start = Offset(center.x + radius * 0.10f, center.y + radius * 0.15f),
        end = Offset(center.x + radius * 0.10f, center.y + radius * 0.62f),
        strokeWidth = radius * 0.13f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawWalkingFigure(center: Offset, radius: Float, color: Color, walkPhase: Float) {
    val headRadius = radius * 0.16f
    // Walking head shifted forward slightly
    val headCenter = Offset(center.x + radius * 0.08f, center.y - radius * 0.45f)
    drawCircle(color = color, radius = headRadius, center = headCenter)

    // Angled body for forward motion
    val bodyPath = Path().apply {
        moveTo(center.x - radius * 0.15f, center.y - radius * 0.22f)
        lineTo(center.x + radius * 0.24f, center.y - radius * 0.20f)
        lineTo(center.x + radius * 0.12f, center.y + radius * 0.16f)
        lineTo(center.x - radius * 0.18f, center.y + radius * 0.16f)
        close()
    }
    drawPath(path = bodyPath, color = color)

    // Swinging Arms
    val armSwing = walkPhase * radius * 0.22f
    drawLine(
        color = color,
        start = Offset(center.x - radius * 0.05f, center.y - radius * 0.15f),
        end = Offset(center.x - radius * 0.25f - armSwing, center.y + radius * 0.08f),
        strokeWidth = radius * 0.11f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = color,
        start = Offset(center.x + radius * 0.15f, center.y - radius * 0.15f),
        end = Offset(center.x + radius * 0.32f + armSwing, center.y + radius * 0.05f),
        strokeWidth = radius * 0.11f,
        cap = StrokeCap.Round
    )

    // Dynamic Stepping Legs
    val legOffset = walkPhase * radius * 0.28f
    // Left Leg
    drawLine(
        color = color,
        start = Offset(center.x - radius * 0.06f, center.y + radius * 0.16f),
        end = Offset(center.x - radius * 0.22f - legOffset, center.y + radius * 0.62f),
        strokeWidth = radius * 0.13f,
        cap = StrokeCap.Round
    )
    // Right Leg
    drawLine(
        color = color,
        start = Offset(center.x + radius * 0.08f, center.y + radius * 0.16f),
        end = Offset(center.x + radius * 0.26f + legOffset, center.y + radius * 0.62f),
        strokeWidth = radius * 0.13f,
        cap = StrokeCap.Round
    )
}
