package com.igames.kids.games.trafficlight.ui

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.igames.kids.core.audio.SoundManager
import com.igames.kids.core.components.CuteButton
import com.igames.kids.core.components.ParentalGateDialog
import com.igames.kids.core.components.TopKidBar
import com.igames.kids.core.theme.KidAmber
import com.igames.kids.core.theme.KidAppleGreen
import com.igames.kids.core.theme.KidBackground
import com.igames.kids.core.theme.KidCandyRed
import com.igames.kids.core.theme.KidDeepBlue
import com.igames.kids.core.theme.KidSkyBlue
import com.igames.kids.core.theme.LampGreenOn
import com.igames.kids.core.theme.LampRedOn
import com.igames.kids.core.theme.LampYellowOn
import com.igames.kids.games.trafficlight.engine.TrafficLightController
import com.igames.kids.games.trafficlight.model.TrafficLightConfig
import com.igames.kids.games.trafficlight.model.TrafficLightState
import com.igames.kids.games.trafficlight.model.TrafficLightStyle
import com.igames.kids.games.trafficlight.ui.components.ClassicLightView
import com.igames.kids.games.trafficlight.ui.components.DigitalCountdownView
import com.igames.kids.games.trafficlight.ui.components.PedestrianLightView
import com.igames.kids.games.trafficlight.ui.components.TrafficLightHousing

import com.igames.kids.core.sensor.MotionScreenAwakeManager
import com.igames.kids.core.util.SystemUIHelper

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

    DisposableEffect(Unit) {
        controller.start()
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
        // Top Navigation Bar
        TopKidBar(
            title = "🚥 红绿灯模拟器",
            onBackClick = {
                soundManager.playButtonTap()
                onNavigateBack()
            },
            onSettingsClick = {
                soundManager.playButtonTap()
                showParentalGate = true
            }
        )

        // Style Switcher Tabs
        val styles = TrafficLightStyle.entries
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

        // Center Traffic Light Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            TrafficLightHousing {
                when (currentConfig.style) {
                    TrafficLightStyle.CLASSIC_3_LAMP -> {
                        ClassicLightView(
                            state = state,
                            isBlinkVisible = isBlinkPhaseVisible,
                            lampSize = 88.dp,
                            onLampClick = { manualState ->
                                soundManager.playButtonTap()
                                controller.setManualState(manualState)
                            }
                        )
                    }
                    TrafficLightStyle.PEDESTRIAN -> {
                        PedestrianLightView(
                            state = state,
                            isBlinkVisible = isBlinkPhaseVisible,
                            lampSize = 110.dp,
                            onLampClick = { manualState ->
                                soundManager.playButtonTap()
                                controller.setManualState(manualState)
                            }
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
                                onLampClick = { manualState ->
                                    soundManager.playButtonTap()
                                    controller.setManualState(manualState)
                                }
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
                text = if (isManualMode) "👮 手动小交警指挥中" else "⏱️ 自动信号灯循环中",
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
