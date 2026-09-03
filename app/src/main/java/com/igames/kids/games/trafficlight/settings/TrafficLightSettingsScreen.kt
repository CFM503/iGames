package com.igames.kids.games.trafficlight.settings

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.igames.kids.core.audio.SoundManager
import com.igames.kids.core.components.CuteButton
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
import com.igames.kids.games.trafficlight.model.TrafficLightConfig
import com.igames.kids.games.trafficlight.model.TrafficLightStyle

@Composable
fun TrafficLightSettingsScreen(
    currentConfig: TrafficLightConfig,
    soundManager: SoundManager,
    onSaveConfig: (TrafficLightConfig) -> Unit,
    onNavigateBack: () -> Unit
) {
    var redDuration by remember { mutableIntStateOf(currentConfig.redDuration) }
    var yellowDuration by remember { mutableIntStateOf(currentConfig.yellowDuration) }
    var greenDuration by remember { mutableIntStateOf(currentConfig.greenDuration) }
    var selectedStyle by remember { mutableStateOf(currentConfig.style) }
    var isVoiceEnabled by remember { mutableStateOf(currentConfig.isVoiceEnabled) }
    var isSoundEnabled by remember { mutableStateOf(currentConfig.isSoundEnabled) }
    var isGreenBlinkEnabled by remember { mutableStateOf(currentConfig.isGreenBlinkEnabled) }
    var isTickSoundEnabled by remember { mutableStateOf(currentConfig.isTickSoundEnabled) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KidBackground)
    ) {
        TopKidBar(
            title = "⚙️ 家长与时间设置",
            onBackClick = {
                soundManager.playButtonTap()
                onNavigateBack()
            }
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card 1: Time Configuration
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "⏱️ 红绿灯时长调节",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = KidDeepBlue
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Green Duration
                    DurationStepper(
                        label = "绿灯通行时长",
                        value = greenDuration,
                        accentColor = LampGreenOn,
                        minValue = 3,
                        maxValue = 60,
                        onValueChange = { greenDuration = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Yellow Duration
                    DurationStepper(
                        label = "黄灯等待时长",
                        value = yellowDuration,
                        accentColor = LampYellowOn,
                        minValue = 1,
                        maxValue = 10,
                        onValueChange = { yellowDuration = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Red Duration
                    DurationStepper(
                        label = "红灯停止时长",
                        value = redDuration,
                        accentColor = LampRedOn,
                        minValue = 3,
                        maxValue = 60,
                        onValueChange = { redDuration = it }
                    )
                }
            }

            // Card 2: Audio & Voice
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "🔊 语音与音效",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = KidDeepBlue
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    SwitchRow(
                        title = "童趣语音播报",
                        subtitle = "播报“红灯停，绿灯行，黄灯等一等”",
                        checked = isVoiceEnabled,
                        onCheckedChange = { isVoiceEnabled = it }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SwitchRow(
                        title = "拟真提示音效",
                        subtitle = "换灯鸣笛声与成功通过提示音",
                        checked = isSoundEnabled,
                        onCheckedChange = { isSoundEnabled = it }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SwitchRow(
                        title = "倒计时滴答声 / 盲道提示音",
                        subtitle = "红绿灯倒计时读秒节奏音",
                        checked = isTickSoundEnabled,
                        onCheckedChange = { isTickSoundEnabled = it }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SwitchRow(
                        title = "绿灯末尾3秒闪烁",
                        subtitle = "提示即将切换为黄灯",
                        checked = isGreenBlinkEnabled,
                        onCheckedChange = { isGreenBlinkEnabled = it }
                    )
                }
            }

            // Card 3: Default Style
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "🎨 默认外观样式",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = KidDeepBlue
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    TrafficLightStyle.entries.forEach { style ->
                        val isSelected = style == selectedStyle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) KidSkyBlue.copy(alpha = 0.15f) else Color.Transparent)
                                .clickable {
                                    soundManager.playButtonTap()
                                    selectedStyle = style
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) KidSkyBlue else Color(0xFFE0E0E0)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = style.title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) KidDeepBlue else Color.Black
                                )
                                Text(
                                    text = style.description,
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }

            // Reset Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                CuteButton(
                    onClick = {
                        soundManager.playButtonTap()
                        redDuration = 10
                        yellowDuration = 3
                        greenDuration = 10
                        selectedStyle = TrafficLightStyle.CLASSIC_3_LAMP
                        isVoiceEnabled = true
                        isSoundEnabled = true
                        isGreenBlinkEnabled = true
                        isTickSoundEnabled = true
                    },
                    backgroundColor = Color(0xFF90A4AE),
                    elevation = 2.dp
                ) {
                    Icon(imageVector = Icons.Default.Restore, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("恢复默认设置", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Bottom Save Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            CuteButton(
                onClick = {
                    soundManager.playButtonTap()
                    soundManager.isSoundEffectsEnabled = isSoundEnabled
                    soundManager.isVoiceEnabled = isVoiceEnabled
                    val newConfig = TrafficLightConfig(
                        redDuration = redDuration,
                        yellowDuration = yellowDuration,
                        greenDuration = greenDuration,
                        style = selectedStyle,
                        isSoundEnabled = isSoundEnabled,
                        isVoiceEnabled = isVoiceEnabled,
                        isGreenBlinkEnabled = isGreenBlinkEnabled,
                        isTickSoundEnabled = isTickSoundEnabled
                    )
                    onSaveConfig(newConfig)
                    onNavigateBack()
                },
                backgroundColor = KidAppleGreen,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "保存并应用",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun DurationStepper(
    label: String,
    value: Int,
    accentColor: Color,
    minValue: Int,
    maxValue: Int,
    onValueChange: (Int) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(
                text = "$value 秒",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = accentColor
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEEEEEE))
                    .clickable {
                        if (value > minValue) onValueChange(value - 1)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Remove, contentDescription = "减", tint = Color.DarkGray)
            }

            Slider(
                value = value.toFloat(),
                onValueChange = { onValueChange(it.toInt()) },
                valueRange = minValue.toFloat()..maxValue.toFloat(),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                colors = SliderDefaults.colors(
                    thumbColor = accentColor,
                    activeTrackColor = accentColor,
                    inactiveTrackColor = accentColor.copy(alpha = 0.2f)
                )
            )

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEEEEEE))
                    .clickable {
                        if (value < maxValue) onValueChange(value + 1)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "加", tint = Color.DarkGray)
            }
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(text = subtitle, fontSize = 12.sp, color = Color.Gray)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = KidAppleGreen
            )
        )
    }
}
