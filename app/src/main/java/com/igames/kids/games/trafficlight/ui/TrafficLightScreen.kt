package com.igames.kids.games.trafficlight.ui

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.igames.kids.core.audio.SoundManager
import com.igames.kids.core.components.CuteButton
import com.igames.kids.core.components.ParentalGateDialog
import com.igames.kids.core.components.TopKidBar
import com.igames.kids.core.sensor.MotionScreenAwakeManager
import com.igames.kids.core.theme.KidAppleGreen
import com.igames.kids.core.theme.KidBackground
import com.igames.kids.core.theme.KidCandyRed
import com.igames.kids.core.theme.KidDeepBlue
import com.igames.kids.core.theme.KidSkyBlue
import com.igames.kids.core.theme.LampGreenOn
import com.igames.kids.core.theme.LampRedOn
import com.igames.kids.core.theme.LampYellowOn
import com.igames.kids.core.util.SystemUIHelper
import com.igames.kids.games.trafficlight.engine.TrafficLightController
import com.igames.kids.games.trafficlight.model.TrafficLightConfig
import com.igames.kids.games.trafficlight.model.TrafficLightState
import com.igames.kids.games.trafficlight.model.TrafficLightStyle
import com.igames.kids.games.trafficlight.ui.components.ClassicLightView
import com.igames.kids.games.trafficlight.ui.components.DigitalCountdownView
import com.igames.kids.games.trafficlight.ui.components.PedestrianLightView
import com.igames.kids.games.trafficlight.ui.components.TrafficLightHousing

/**
 * Custom gesture modifier that cleanly distinguishes between:
 * - Single click/tap: triggers manual light change
 * - Horizontal swipe left: switches to next light style
 * - Horizontal swipe right: switches to previous light style
 */
private fun Modifier.trafficLightGesture(
    onTap: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit
): Modifier = this.pointerInput(Unit) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        var totalDragX = 0f
        var totalDragY = 0f
        var isDrag = false
        while (true) {
            val event = awaitPointerEvent()
            val change = event.changes.firstOrNull { it.id == down.id } ?: continue
            if (change.changedToUp()) {
                if (isDrag) {
                    val threshold = 50.dp.toPx()
                    if (totalDragX < -threshold) {
                        onSwipeLeft()
                    } else if (totalDragX > threshold) {
                        onSwipeRight()
                    }
                } else {
                    onTap()
                }
                break
            }
            val drag = change.positionChange()
            totalDragX += drag.x
            totalDragY += drag.y
            if (Math.abs(totalDragX) > 18.dp.toPx() && Math.abs(totalDragX) > Math.abs(totalDragY)) {
                isDrag = true
                change.consume()
            }
        }
    }
}

@Composable
fun TrafficLightScreen(
    soundManager: SoundManager,
    currentConfig: TrafficLightConfig,
    onNavigateBack: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToGame: () -> Unit,
    onStyleChange: (TrafficLightStyle) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Full screen immersive and intelligent motion awake management
    DisposableEffect(Unit) {
        val activity = context as? Activity
        SystemUIHelper.enterFullScreen(activity)
        val motionAwakeManager = activity?.let { MotionScreenAwakeManager(it) }
        motionAwakeManager?.start()
        onDispose {
            motionAwakeManager?.stop()
        }
    }

    var showParentalGate by remember { mutableStateOf(false) }
    var isMaximized by remember { mutableStateOf(false) }

    val controller = remember {
        TrafficLightController(
            scope = coroutineScope,
            initialConfig = currentConfig,
            onStateChanged = { state ->
                when (state) {
                    TrafficLightState.GREEN -> soundManager.playGreenAlert()
                    TrafficLightState.YELLOW -> soundManager.playYellowAlert()
                    TrafficLightState.RED -> soundManager.playRedAlert()
                    TrafficLightState.GREEN_BLINK -> soundManager.playTick()
                }
            },
            onTick = { sec, state ->
                if (currentConfig.isTickSoundEnabled) {
                    if (currentConfig.style == TrafficLightStyle.PEDESTRIAN && state.isGreen) {
                        soundManager.playPedestrianBeep()
                    } else {
                        soundManager.playTick()
                    }
                }
            }
        )
    }

    // Keep controller updated with latest config
    DisposableEffect(currentConfig) {
        controller.config = currentConfig
        onDispose { }
    }

    // Always start with RED light on screen entry
    DisposableEffect(Unit) {
        controller.start(forceRed = true)
        onDispose {
            controller.stop()
        }
    }

    val state by controller.currentState.collectAsState()
    val remainingSec by controller.remainingSeconds.collectAsState()
    val isBlinkPhaseVisible by controller.isBlinkPhaseVisible.collectAsState()
    val isPaused by controller.isPaused.collectAsState()
    val isManualMode by controller.isManualMode.collectAsState()

    var isMuted by remember { mutableStateOf(!soundManager.isSoundEffectsEnabled && !soundManager.isVoiceEnabled) }

    // Helpers for switching styles via swipe
    val styles = TrafficLightStyle.entries
    val onSwipeToNextStyle = {
        soundManager.playButtonTap()
        val currentIndex = styles.indexOf(currentConfig.style)
        val nextStyle = styles[(currentIndex + 1) % styles.size]
        onStyleChange(nextStyle)
        controller.resetToRed()
    }
    val onSwipeToPrevStyle = {
        soundManager.playButtonTap()
        val currentIndex = styles.indexOf(currentConfig.style)
        val prevStyle = styles[(currentIndex - 1 + styles.size) % styles.size]
        onStyleChange(prevStyle)
        controller.resetToRed()
    }
    val onScreenTapToCycleLight = {
        soundManager.playButtonTap()
        controller.cycleNextManualState()
    }

    if (isMaximized) {
        // Fullscreen Giant Traffic Light Mode (让红绿灯贴紧屏幕边缘，超大尺寸展现)
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0C0F14))
                .trafficLightGesture(
                    onTap = onScreenTapToCycleLight,
                    onSwipeLeft = onSwipeToNextStyle,
                    onSwipeRight = onSwipeToPrevStyle
                ),
            contentAlignment = Alignment.Center
        ) {
            val screenW = maxWidth
            val screenH = maxHeight

            // Edge-to-edge Traffic Light Housing hugging screen borders
            TrafficLightHousing(
                modifier = Modifier
                    .padding(horizontal = 2.dp, vertical = 4.dp)
                    .fillMaxWidth(0.98f),
                innerPadding = 4.dp
            ) {
                when (currentConfig.style) {
                    TrafficLightStyle.CLASSIC_3_LAMP -> {
                        // Maximize 3 lamps dynamically to fill screen width and height
                        val classicLampSize = minOf(screenW * 0.70f, (screenH - 70.dp) / 3.35f)
                        ClassicLightView(
                            state = state,
                            isBlinkVisible = isBlinkPhaseVisible,
                            lampSize = classicLampSize,
                            onLampClick = { onScreenTapToCycleLight() }
                        )
                    }
                    TrafficLightStyle.PEDESTRIAN -> {
                        // Giant pedestrian lamps up to 260.dp ~ 280.dp!
                        val pedLampSize = minOf(screenW * 0.88f, (screenH - 70.dp) / 2.25f)
                        PedestrianLightView(
                            state = state,
                            isBlinkVisible = isBlinkPhaseVisible,
                            lampSize = pedLampSize,
                            onLampClick = { onScreenTapToCycleLight() }
                        )
                    }
                    TrafficLightStyle.DIGITAL_COUNTDOWN -> {
                        // Giant countdown circle up to 340.dp ~ 370.dp!
                        val countdownSize = minOf(screenW * 0.94f, screenH * 0.65f)
                        DigitalCountdownView(
                            state = state,
                            remainingSeconds = remainingSec,
                            totalSeconds = when {
                                state.isRed -> currentConfig.redDuration
                                state.isYellow -> currentConfig.yellowDuration
                                else -> currentConfig.greenDuration
                            },
                            size = countdownSize
                        )
                    }
                    TrafficLightStyle.VEHICLE_WITH_TIMER -> {
                        val horizontalLampSize = minOf((screenW - 50.dp) / 3.4f, 105.dp)
                        val bottomTimerSize = minOf(screenW * 0.65f, screenH * 0.35f)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            ClassicLightView(
                                state = state,
                                isBlinkVisible = isBlinkPhaseVisible,
                                lampSize = horizontalLampSize,
                                isHorizontal = true,
                                onLampClick = { onScreenTapToCycleLight() }
                            )
                            DigitalCountdownView(
                                state = state,
                                remainingSeconds = remainingSec,
                                totalSeconds = when {
                                    state.isRed -> currentConfig.redDuration
                                    state.isYellow -> currentConfig.yellowDuration
                                    else -> currentConfig.greenDuration
                                },
                                size = bottomTimerSize,
                                showStatusHint = false
                            )
                        }
                    }
                }
            }

            // Top Left Floating Status Badge & Auto Restore
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.70f))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isManualMode) "👮 手动指挥中" else "⏱️ 自动循环中",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isManualMode) KidCandyRed else KidAppleGreen
                )
                if (isManualMode) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "（恢复自动）",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = KidSkyBlue,
                        modifier = Modifier.clickable {
                            soundManager.playButtonTap()
                            controller.switchToAutoMode()
                        }
                    )
                }
            }

            // Top Right Floating Exit Maximize Button & Style Badge
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black.copy(alpha = 0.70f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = currentConfig.style.title,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(4.dp, CircleShape)
                        .background(Color.White.copy(alpha = 0.85f), CircleShape)
                        .clip(CircleShape)
                        .clickable {
                            soundManager.playButtonTap()
                            isMaximized = false
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FullscreenExit,
                        contentDescription = "退出全屏",
                        tint = KidDeepBlue,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            // Bottom Floating Hint
            Text(
                text = "👆 单击切灯 | 👈👉 左右划动切样式",
                color = Color.White.copy(alpha = 0.90f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black.copy(alpha = 0.65f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    } else {
        // Standard Mode
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            KidBackground,
                            Color(0xFFE8F0F8)
                        )
                    )
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Navigation Bar with Maximize Button
            TopKidBar(
                title = "🚥 红绿灯模拟器",
                onBackClick = {
                    soundManager.playButtonTap()
                    onNavigateBack()
                },
                onMaximizeClick = {
                    soundManager.playButtonTap()
                    isMaximized = true
                },
                onSettingsClick = {
                    soundManager.playButtonTap()
                    showParentalGate = true
                }
            )

            // Style Switcher Tabs
            val selectedIndex = styles.indexOf(currentConfig.style)
            ScrollableTabRow(
                selectedTabIndex = if (selectedIndex >= 0) selectedIndex else 0,
                edgePadding = 16.dp,
                containerColor = Color.Transparent,
                contentColor = KidDeepBlue,
                divider = {}
            ) {
                styles.forEachIndexed { index, style ->
                    val isSelected = style == currentConfig.style
                    Tab(
                        selected = isSelected,
                        onClick = {
                            soundManager.playButtonTap()
                            onStyleChange(style)
                            controller.resetToRed()
                        },
                        text = {
                            Text(
                                text = style.title,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                fontSize = 15.sp,
                                color = if (isSelected) KidDeepBlue else Color.Gray
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Center Traffic Light Area - Swipe to change style or tap to cycle light
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .trafficLightGesture(
                        onTap = onScreenTapToCycleLight,
                        onSwipeLeft = onSwipeToNextStyle,
                        onSwipeRight = onSwipeToPrevStyle
                    ),
                contentAlignment = Alignment.Center
            ) {
                TrafficLightHousing {
                    when (currentConfig.style) {
                        TrafficLightStyle.CLASSIC_3_LAMP -> {
                            ClassicLightView(
                                state = state,
                                isBlinkVisible = isBlinkPhaseVisible,
                                lampSize = 88.dp,
                                onLampClick = { onScreenTapToCycleLight() }
                            )
                        }
                        TrafficLightStyle.PEDESTRIAN -> {
                            PedestrianLightView(
                                state = state,
                                isBlinkVisible = isBlinkPhaseVisible,
                                lampSize = 110.dp,
                                onLampClick = { onScreenTapToCycleLight() }
                            )
                        }
                        TrafficLightStyle.DIGITAL_COUNTDOWN -> {
                            val totalSec = when {
                                state.isRed -> currentConfig.redDuration
                                state.isYellow -> currentConfig.yellowDuration
                                else -> currentConfig.greenDuration
                            }
                            DigitalCountdownView(
                                state = state,
                                remainingSeconds = remainingSec,
                                totalSeconds = totalSec,
                                size = 180.dp
                            )
                        }
                        TrafficLightStyle.VEHICLE_WITH_TIMER -> {
                            val totalSec = when {
                                state.isRed -> currentConfig.redDuration
                                state.isYellow -> currentConfig.yellowDuration
                                else -> currentConfig.greenDuration
                            }
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ClassicLightView(
                                    state = state,
                                    isBlinkVisible = isBlinkPhaseVisible,
                                    lampSize = 64.dp,
                                    isHorizontal = true,
                                    onLampClick = { onScreenTapToCycleLight() }
                                )
                                DigitalCountdownView(
                                    state = state,
                                    remainingSeconds = remainingSec,
                                    totalSeconds = totalSec,
                                    size = 120.dp,
                                    showStatusHint = false
                                )
                            }
                        }
                    }
                }
            }

            // Mode Status Bar
            Row(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.8f))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isManualMode) "👮 手动小交警指挥中" else "⏱️ 自动信号灯循环中（点击切灯/左右划动）",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isManualMode) KidCandyRed else KidAppleGreen
                )
                if (isManualMode) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "（恢复自动）",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = KidSkyBlue,
                        modifier = Modifier.clickable {
                            soundManager.playButtonTap()
                            controller.switchToAutoMode()
                        }
                    )
                }
            }

            // Bottom Controls Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Manual Lamp Triggers for Role-Playing
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Red Button
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .shadow(4.dp, CircleShape)
                            .background(LampRedOn, CircleShape)
                            .clip(CircleShape)
                            .clickable {
                                soundManager.playButtonTap()
                                controller.setManualState(TrafficLightState.RED)
                                soundManager.playManualPoliceSpeech("红")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("红", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }
                    // Yellow Button (only shown in vehicle modes, pedestrian mode has no yellow light)
                    if (currentConfig.style != TrafficLightStyle.PEDESTRIAN) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .shadow(4.dp, CircleShape)
                                .background(LampYellowOn, CircleShape)
                                .clip(CircleShape)
                                .clickable {
                                    soundManager.playButtonTap()
                                    controller.setManualState(TrafficLightState.YELLOW)
                                    soundManager.playManualPoliceSpeech("黄")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("黄", color = Color(0xFF3E2723), fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                    }
                    // Green Button
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .shadow(4.dp, CircleShape)
                            .background(LampGreenOn, CircleShape)
                            .clip(CircleShape)
                            .clickable {
                                soundManager.playButtonTap()
                                controller.setManualState(TrafficLightState.GREEN)
                                soundManager.playManualPoliceSpeech("绿")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("绿", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }
                }

                // Audio Mute Toggle
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(3.dp, CircleShape)
                        .background(Color.White, CircleShape)
                        .clip(CircleShape)
                        .clickable {
                            isMuted = !isMuted
                            soundManager.isSoundEffectsEnabled = !isMuted
                            soundManager.isVoiceEnabled = !isMuted
                            if (!isMuted) soundManager.playButtonTap()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = "静音切换",
                        tint = if (isMuted) Color.Gray else KidSkyBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Go to Cross-Road Game
                CuteButton(
                    onClick = {
                        soundManager.playButtonTap()
                        onNavigateToGame()
                    },
                    backgroundColor = KidAppleGreen,
                    cornerRadius = 20.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "过马路游戏",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showParentalGate) {
        ParentalGateDialog(
            onDismiss = { showParentalGate = false },
            onSuccess = {
                showParentalGate = false
                onNavigateToSettings()
            }
        )
    }
}
