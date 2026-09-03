package com.igames.kids.games.trafficlight.ui.components

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.igames.kids.core.theme.LampGreenGlow
import com.igames.kids.core.theme.LampGreenOff
import com.igames.kids.core.theme.LampGreenOn
import com.igames.kids.core.theme.LampRedGlow
import com.igames.kids.core.theme.LampRedOff
import com.igames.kids.core.theme.LampRedOn
import com.igames.kids.core.theme.LampVisorColor
import com.igames.kids.core.theme.LampYellowGlow
import com.igames.kids.core.theme.LampYellowOff
import com.igames.kids.core.theme.LampYellowOn
import com.igames.kids.games.trafficlight.model.TrafficLightState

@Composable
fun ClassicLightView(
    state: TrafficLightState,
    isBlinkVisible: Boolean,
    lampSize: Dp = 100.dp,
    isHorizontal: Boolean = false,
    onLampClick: ((TrafficLightState) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isRedActive = state.isRed
    val isYellowActive = state.isYellow
    val isGreenActive = if (state == TrafficLightState.GREEN_BLINK) isBlinkVisible else state.isGreen

    if (isHorizontal) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TrafficLamp(
                colorOn = LampRedOn,
                colorOff = LampRedOff,
                glowColor = LampRedGlow,
                isOn = isRedActive,
                size = lampSize,
                onClick = { onLampClick?.invoke(TrafficLightState.RED) }
            )
            TrafficLamp(
                colorOn = LampYellowOn,
                colorOff = LampYellowOff,
                glowColor = LampYellowGlow,
                isOn = isYellowActive,
                size = lampSize,
                onClick = { onLampClick?.invoke(TrafficLightState.YELLOW) }
            )
            TrafficLamp(
                colorOn = LampGreenOn,
                colorOff = LampGreenOff,
                glowColor = LampGreenGlow,
                isOn = isGreenActive,
                size = lampSize,
                onClick = { onLampClick?.invoke(TrafficLightState.GREEN) }
            )
        }
    } else {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TrafficLamp(
                colorOn = LampRedOn,
                colorOff = LampRedOff,
                glowColor = LampRedGlow,
                isOn = isRedActive,
                size = lampSize,
                onClick = { onLampClick?.invoke(TrafficLightState.RED) }
            )
            TrafficLamp(
                colorOn = LampYellowOn,
                colorOff = LampYellowOff,
                glowColor = LampYellowGlow,
                isOn = isYellowActive,
                size = lampSize,
                onClick = { onLampClick?.invoke(TrafficLightState.YELLOW) }
            )
            TrafficLamp(
                colorOn = LampGreenOn,
                colorOff = LampGreenOff,
                glowColor = LampGreenGlow,
                isOn = isGreenActive,
                size = lampSize,
                onClick = { onLampClick?.invoke(TrafficLightState.GREEN) }
            )
        }
    }
}

@Composable
fun TrafficLamp(
    colorOn: Color,
    colorOff: Color,
    glowColor: Color,
    isOn: Boolean,
    size: Dp,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val glowAlpha by animateFloatAsState(
        targetValue = if (isOn) 1.0f else 0.0f,
        animationSpec = tween(durationMillis = 150),
        label = "glowAlpha"
    )

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

            // Outer Sun Visor (遮阳帽檐)
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

            // Outer Glow when lit
            if (glowAlpha > 0f) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            glowColor.copy(alpha = 0.8f * glowAlpha),
                            glowColor.copy(alpha = 0.3f * glowAlpha),
                            Color.Transparent
                        ),
                        center = center,
                        radius = radius * 1.55f
                    ),
                    radius = radius * 1.55f,
                    center = center
                )
            }

            // Dark bezel ring around lamp
            drawCircle(
                color = Color(0xFF101316),
                radius = radius * 1.08f,
                center = center
            )
            drawCircle(
                color = Color(0xFF32383E),
                radius = radius * 1.05f,
                center = center,
                style = Stroke(width = 3f)
            )

            // Lens Glass Body
            val lensColor = if (isOn) colorOn else colorOff
            drawCircle(
                brush = Brush.radialGradient(
                    colors = if (isOn) {
                        listOf(
                            Color.White.copy(alpha = 0.85f),
                            lensColor,
                            Color(
                                red = lensColor.red * 0.7f,
                                green = lensColor.green * 0.7f,
                                blue = lensColor.blue * 0.7f,
                                alpha = lensColor.alpha
                            )
                        )
                    } else {
                        listOf(
                            lensColor.copy(alpha = 0.8f),
                            lensColor,
                            Color(0xFF0F1113)
                        )
                    },
                    center = Offset(center.x - radius * 0.25f, center.y - radius * 0.25f),
                    radius = radius
                ),
                radius = radius,
                center = center
            )

            // Honeycomb lens grid texture
            val gridStep = radius * 0.28f
            var gx = center.x - radius
            while (gx <= center.x + radius) {
                var gy = center.y - radius
                while (gy <= center.y + radius) {
                    val dist = (Offset(gx, gy) - center).getDistance()
                    if (dist < radius * 0.88f) {
                        drawCircle(
                            color = if (isOn) Color.White.copy(alpha = 0.25f) else Color.Black.copy(alpha = 0.3f),
                            radius = radius * 0.06f,
                            center = Offset(gx, gy)
                        )
                    }
                    gy += gridStep
                }
                gx += gridStep
            }

            // Glossy Glass Reflection Arc (高光弧线)
            drawArc(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = if (isOn) 0.5f else 0.25f),
                        Color.Transparent
                    )
                ),
                startAngle = 200f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(center.x - radius * 0.75f, center.y - radius * 0.85f),
                size = Size(radius * 1.5f, radius * 0.85f),
                style = Stroke(width = radius * 0.12f)
            )
        }
    }
}
