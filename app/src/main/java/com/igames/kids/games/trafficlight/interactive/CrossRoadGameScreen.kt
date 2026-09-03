package com.igames.kids.games.trafficlight.interactive

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.igames.kids.core.audio.SoundManager
import com.igames.kids.core.components.TopKidBar
import com.igames.kids.core.theme.KidAmber
import com.igames.kids.core.theme.KidAppleGreen
import com.igames.kids.core.theme.KidBackground
import com.igames.kids.core.theme.KidCandyRed
import com.igames.kids.core.theme.KidDeepBlue
import com.igames.kids.core.theme.KidSkyBlue
import com.igames.kids.core.theme.KidSunYellow
import com.igames.kids.core.theme.LampGreenOn
import com.igames.kids.core.theme.LampRedOn
import com.igames.kids.core.theme.LampYellowOn
import com.igames.kids.games.trafficlight.engine.TrafficLightController
import com.igames.kids.games.trafficlight.model.TrafficLightConfig
import com.igames.kids.games.trafficlight.model.TrafficLightState
import com.igames.kids.games.trafficlight.ui.components.ClassicLightView
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun CrossRoadGameScreen(
    soundManager: SoundManager,
    config: TrafficLightConfig,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    val controller = remember {
        TrafficLightController(
            scope = coroutineScope,
            initialConfig = config,
            onStateChanged = { state ->
                when (state) {
                    TrafficLightState.GREEN -> soundManager.playGreenAlert()
                    TrafficLightState.YELLOW -> soundManager.playYellowAlert()
                    TrafficLightState.RED -> soundManager.playRedAlert()
                    TrafficLightState.GREEN_BLINK -> soundManager.playTick()
                }
            }
        )
    }

    DisposableEffect(Unit) {
        controller.start()
        onDispose { controller.stop() }
    }

    val state by controller.currentState.collectAsState()
    val remainingSec by controller.remainingSeconds.collectAsState()
    val isBlinkPhaseVisible by controller.isBlinkPhaseVisible.collectAsState()

    var starCount by remember { mutableIntStateOf(0) }
    var carProgress by remember { mutableFloatStateOf(0.05f) } // 0.05f (start) to 0.90f (finish)
    var isHoldingGas by remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf<String?>("绿灯快快走，红灯快快停！") }

    // Driving physics loop
    LaunchedEffect(Unit) {
        while (isActive) {
            if (isHoldingGas) {
                when (state) {
                    TrafficLightState.GREEN, TrafficLightState.GREEN_BLINK -> {
                        alertMessage = "🚗 绿灯通行，加油向前开！"
                        carProgress = (carProgress + 0.015f).coerceAtMost(0.92f)
                        if (carProgress >= 0.90f) {
                            // Reached Destination!
                            starCount++
                            soundManager.playSuccess()
                            alertMessage = "🎉 太棒啦！成功过马路！+1 星星 ⭐"
                            delay(1200)
                            carProgress = 0.05f
                            alertMessage = "准备下一次过马路！"
                        }
                    }
                    TrafficLightState.YELLOW -> {
                        alertMessage = "⚠️ 黄灯了，小心减速！"
                        carProgress = (carProgress + 0.006f).coerceAtMost(0.92f)
                    }
                    TrafficLightState.RED -> {
                        // Violated red light!
                        soundManager.playWarning()
                        alertMessage = "🚨 哎呀！红灯不能走！退回起点！"
                        carProgress = 0.05f
                        delay(1000)
                    }
                }
            }
            delay(50)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KidBackground),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopKidBar(
            title = "🚗 小交警过马路",
            onBackClick = {
                soundManager.playButtonTap()
                onNavigateBack()
            }
        )

        // Status Card: Score & Active Light
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(3.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Stars Collected
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "星星",
                        tint = KidSunYellow,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "星星: $starCount",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = KidAmber
                    )
                }

                // Compact Mini Traffic Light View
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ClassicLightView(
                        state = state,
                        isBlinkVisible = isBlinkPhaseVisible,
                        lampSize = 28.dp,
                        isHorizontal = true
                    )
                    Text(
                        text = "${remainingSec}s",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = when {
                            state.isRed -> LampRedOn
                            state.isYellow -> LampYellowOn
                            else -> LampGreenOn
                        }
                    )
                }
            }
        }

        // Educational Voice Prompt Bubble
        if (alertMessage != null) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = alertMessage ?: "",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = KidDeepBlue
                )
            }
        }

        // Road & Zebra Crossing Canvas Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF37474F)),
            contentAlignment = Alignment.CenterStart
        ) {
            // Road markings & Zebra Crossing
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Sidewalk borders (Top & Bottom curbs)
                drawRect(color = Color(0xFFCFD8DC), topLeft = Offset(0f, 0f), size = Size(w, h * 0.12f))
                drawRect(color = Color(0xFFCFD8DC), topLeft = Offset(0f, h * 0.88f), size = Size(w, h * 0.12f))

                // Zebra Crossing Stripes (斑马线)
                val stripeWidth = w * 0.045f
                val stripeHeight = h * 0.65f
                val stripeGap = w * 0.035f
                var x = w * 0.25f
                while (x < w * 0.82f) {
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.9f),
                        topLeft = Offset(x, h * 0.17f),
                        size = Size(stripeWidth, stripeHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                    )
                    x += (stripeWidth + stripeGap)
                }

                // Finish Line Flag / Destination Area
                drawCircle(
                    color = KidSunYellow.copy(alpha = 0.4f),
                    radius = h * 0.26f,
                    center = Offset(w * 0.90f, h * 0.5f)
                )
                drawCircle(
                    color = KidSunYellow,
                    radius = h * 0.18f,
                    center = Offset(w * 0.90f, h * 0.5f)
                )
            }

            // Target Finish Icon (Star goal)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "终点",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Cartoon Little Car (Player)
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset {
                            // Calculate offset based on carProgress
                            IntOffset(
                                x = (carProgress * 700).dp.roundToPx(),
                                y = 0
                            )
                        }
                        .size(56.dp)
                        .shadow(8.dp, CircleShape)
                        .background(KidCandyRed, CircleShape)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = "小汽车",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        // Bottom Big Interactive Driving Button
        val buttonScale by animateFloatAsState(
            targetValue = if (isHoldingGas) 0.9f else 1.0f,
            animationSpec = spring(dampingRatio = 0.5f),
            label = "gasScale"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .scale(buttonScale)
                    .fillMaxWidth()
                    .height(72.dp)
                    .shadow(8.dp, RoundedCornerShape(36.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = if (isHoldingGas) {
                                listOf(KidDeepGreen, KidAppleGreen)
                            } else {
                                listOf(KidAppleGreen, KidSkyBlue)
                            }
                        ),
                        shape = RoundedCornerShape(36.dp)
                    )
                    .pointerInput(Unit) {
                        while (true) {
                            awaitPointerEventScope {
                                awaitFirstDown(false)
                                isHoldingGas = true
                                waitForUpOrCancellation()
                                isHoldingGas = false
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (isHoldingGas) "正在前进中… 松开即停" else "按住出发！过马路啦",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }
    }
}
