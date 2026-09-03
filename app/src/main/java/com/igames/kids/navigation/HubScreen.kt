package com.igames.kids.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.FilterDrama
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.igames.kids.core.audio.SoundManager
import com.igames.kids.core.components.ParentalGateDialog
import com.igames.kids.core.theme.KidAmber
import com.igames.kids.core.theme.KidAppleGreen
import com.igames.kids.core.theme.KidBackground
import com.igames.kids.core.theme.KidCandyRed
import com.igames.kids.core.theme.KidDeepBlue
import com.igames.kids.core.theme.KidPurple
import com.igames.kids.core.theme.KidSkyBlue
import com.igames.kids.core.theme.KidSoftOrange
import com.igames.kids.core.theme.KidSunYellow

@Composable
fun HubScreen(
    soundManager: SoundManager,
    onOpenTrafficLight: () -> Unit,
    onOpenSettings: () -> Unit
) {
    var showParentalGate by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "sunBounce")
    val sunScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sunScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        KidSkyBlue.copy(alpha = 0.35f),
                        KidBackground
                    )
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Cheerful Header Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.WbSunny,
                    contentDescription = "小太阳",
                    tint = KidAmber,
                    modifier = Modifier
                        .size(44.dp)
                        .scale(sunScale)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "iGames 启蒙乐园",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = KidDeepBlue
                    )
                    Text(
                        text = "快乐探索 · 趣味启蒙",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                }
            }

            // Parental settings button
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .shadow(3.dp, CircleShape)
                    .background(Color.White, CircleShape)
                    .clip(CircleShape)
                    .clickable {
                        soundManager.playButtonTap()
                        showParentalGate = true
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "设置",
                    tint = KidSkyBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Game Collection Cards Container
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card 1: Traffic Light Game (Active!)
            GameCollectionCard(
                title = "🚥 红绿灯模拟与互动",
                subtitle = "真实仿真信号灯 · 小交警过马路",
                tag = "🔥 热门推荐",
                tagBg = KidCandyRed,
                accentColor = KidAppleGreen,
                icon = Icons.Default.Traffic,
                isAvailable = true,
                onClick = {
                    soundManager.playButtonTap()
                    onOpenTrafficLight()
                }
            )

            // Card 2: Color & Shapes (Future slot)
            GameCollectionCard(
                title = "🎨 颜色与形状乐园",
                subtitle = "认识圆形三角形 · 趣味拼图配对",
                tag = "⭐ 即将推出",
                tagBg = KidPurple,
                accentColor = KidPurple,
                icon = Icons.Default.Category,
                isAvailable = false,
                onClick = {
                    soundManager.speak("这个小游戏正在制作中哦！")
                }
            )

            // Card 3: Animal Sounds (Future slot)
            GameCollectionCard(
                title = "🦁 神奇动物声音",
                subtitle = "辨识各种动物叫声 · 森林探索启蒙",
                tag = "⭐ 即将推出",
                tagBg = KidSoftOrange,
                accentColor = KidSoftOrange,
                icon = Icons.Default.Pets,
                isAvailable = false,
                onClick = {
                    soundManager.speak("动物朋友们很快就来啦！")
                }
            )

            // Card 4: Counting (Future slot)
            GameCollectionCard(
                title = "🔢 趣味数数乐园",
                subtitle = "123 数小鸭 · 基础数字启蒙",
                tag = "⭐ 即将推出",
                tagBg = KidSkyBlue,
                accentColor = KidDeepBlue,
                icon = Icons.Default.AutoAwesome,
                isAvailable = false,
                onClick = {
                    soundManager.speak("数数小游戏正在准备哦！")
                }
            )
        }
    }

    if (showParentalGate) {
        ParentalGateDialog(
            onDismiss = { showParentalGate = false },
            onSuccess = {
                showParentalGate = false
                onOpenSettings()
            }
        )
    }
}

@Composable
fun GameCollectionCard(
    title: String,
    subtitle: String,
    tag: String,
    tagBg: Color,
    accentColor: Color,
    icon: ImageVector,
    isAvailable: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isAvailable) 6.dp else 2.dp,
                shape = RoundedCornerShape(24.dp)
            )
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isAvailable) Color.White else Color(0xFFF9FAFB)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Badge
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .shadow(4.dp, RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                accentColor.copy(alpha = if (isAvailable) 1f else 0.5f),
                                accentColor.copy(alpha = if (isAvailable) 0.8f else 0.3f)
                            )
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text Info
            Column(modifier = Modifier.weight(1f)) {
                // Tag badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(tagBg.copy(alpha = if (isAvailable) 0.15f else 0.08f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = tag,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = tagBg.copy(alpha = if (isAvailable) 1f else 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isAvailable) KidDeepBlue else Color.Gray
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = if (isAvailable) Color.Gray else Color.LightGray
                )
            }

            // Play Icon
            if (isAvailable) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(KidAppleGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "进入游戏",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
