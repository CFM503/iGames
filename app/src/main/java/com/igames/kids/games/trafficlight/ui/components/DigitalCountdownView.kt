package com.igames.kids.games.trafficlight.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.igames.kids.core.theme.LampGreenGlow
import com.igames.kids.core.theme.LampGreenOn
import com.igames.kids.core.theme.LampRedGlow
import com.igames.kids.core.theme.LampRedOn
import com.igames.kids.core.theme.LampYellowGlow
import com.igames.kids.core.theme.LampYellowOn
import com.igames.kids.games.trafficlight.model.TrafficLightState

@Composable
fun DigitalCountdownView(
    state: TrafficLightState,
    remainingSeconds: Int,
    totalSeconds: Int,
    size: Dp = 160.dp,
    showStatusHint: Boolean = true,
    modifier: Modifier = Modifier
) {
    val targetColor = when {
        state.isRed -> LampRedOn
        state.isYellow -> LampYellowOn
        else -> LampGreenOn
    }
    val glowColor = when {
        state.isRed -> LampRedGlow
        state.isYellow -> LampYellowGlow
        else -> LampGreenGlow
    }
    val hintText = when {
        state.isRed -> "红灯停"
        state.isYellow -> "黄灯等"
        else -> "绿灯行"
    }

    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(300),
        label = "colorAnim"
    )

    val progress = if (totalSeconds > 0) {
        (remainingSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(500),
        label = "progressAnim"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(size)) {
                val center = Offset(this.size.width / 2f, this.size.height / 2f)
                val radius = this.size.minDimension / 2.2f

                // Outer casing background
                drawCircle(
                    color = Color(0xFF14171A),
                    radius = radius * 1.05f,
                    center = center
                )

                // Track ring
                drawCircle(
                    color = Color(0xFF222830),
                    radius = radius * 0.9f,
                    center = center,
                    style = Stroke(width = 10f)
                )

                // Outer soft glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            glowColor.copy(alpha = 0.35f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = radius * 1.3f
                    ),
                    radius = radius * 1.3f,
                    center = center
                )

                // Circular Progress Arc
                val sweep = animatedProgress * 360f
                drawArc(
                    color = animatedColor,
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(center.x - radius * 0.9f, center.y - radius * 0.9f),
                    size = Size(radius * 1.8f, radius * 1.8f),
                    style = Stroke(width = 12f, cap = StrokeCap.Round)
                )
            }

            // Digital Number Display
            Text(
                text = String.format("%02d", remainingSeconds),
                fontSize = (size.value * 0.36f).sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                color = animatedColor
            )
        }

        if (showStatusHint) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = hintText,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = animatedColor
            )
        }
    }
}
