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
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Sanitizer
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Toys
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.igames.kids.core.update.UpdateDialog
import com.igames.kids.core.update.UpdateManager

enum class HabitCategory(val title: String) {
    ALL("全部规范"),
    SAFETY("安全规则"),
    HYGIENE("自理卫生"),
    MANNERS("习惯礼仪")
}

@Composable
fun HubScreen(
    soundManager: SoundManager,
    updateManager: UpdateManager,
    onOpenTrafficLight: () -> Unit,
    onOpenSettings: () -> Unit
) {
    var showParentalGate by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(HabitCategory.ALL) }

    val updateInfo by updateManager.updateInfo.collectAsState()

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
                        text = "iGames 习惯乐园",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = KidDeepBlue
                    )
                    Text(
                        text = "日常生活规范 · 趣味好习惯启蒙",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                }
            }

            // Parental settings button (with red dot badge if update available)
            Box(
                modifier = Modifier
                    .size(48.dp)
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
                    modifier = Modifier.size(26.dp)
                )
                if (updateInfo.hasUpdate) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .align(Alignment.TopEnd)
                            .background(KidCandyRed, CircleShape)
                    )
                }
            }
        }

        // Online Update Notification Card Banner
        if (updateInfo.hasUpdate) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp)
                    .clickable {
                        soundManager.playButtonTap()
                        showUpdateDialog = true
                    },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = KidAppleGreen),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.RocketLaunch, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "🚀 发现新版本 v${updateInfo.latestVersionName}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "包含全新日常生活规范小游戏，点击升级！",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(text = "立即更新", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = KidAppleGreen)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HabitCategory.entries.forEach { category ->
                val isSelected = category == selectedCategory
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        soundManager.playButtonTap()
                        selectedCategory = category
                    },
                    label = {
                        Text(
                            text = category.title,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = KidSkyBlue,
                        selectedLabelColor = Color.White,
                        containerColor = Color.White,
                        labelColor = Color.DarkGray
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Daily Routine & Habit Norms Game Collection Cards
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Traffic Light (Safety) - ACTIVE!
            if (selectedCategory == HabitCategory.ALL || selectedCategory == HabitCategory.SAFETY) {
                HabitGameCard(
                    title = "🚥 红绿灯小交警",
                    subtitle = "交通安全规范 · 认识红黄绿灯 · 小司机过马路",
                    tag = "🔥 热门体验",
                    tagBg = KidCandyRed,
                    accentColor = KidAppleGreen,
                    icon = Icons.Default.Traffic,
                    isAvailable = true,
                    onClick = {
                        soundManager.playButtonTap()
                        onOpenTrafficLight()
                    }
                )
            }

            // 2. Teeth Brushing (Hygiene)
            if (selectedCategory == HabitCategory.ALL || selectedCategory == HabitCategory.HYGIENE) {
                HabitGameCard(
                    title = "🪥 刷牙小标兵",
                    subtitle = "个人卫生规范 · 3分钟早晚刷牙 · 消灭蛀牙小细菌",
                    tag = "⭐ 即将上线",
                    tagBg = KidSkyBlue,
                    accentColor = KidDeepBlue,
                    icon = Icons.Default.CleaningServices,
                    isAvailable = false,
                    onClick = {
                        soundManager.speak("刷牙小游戏正在制作中，拿起牙刷保护牙齿哦！")
                    }
                )
            }

            // 3. Hand Washing (Hygiene)
            if (selectedCategory == HabitCategory.ALL || selectedCategory == HabitCategory.HYGIENE) {
                HabitGameCard(
                    title = "🧼 洗手七步法",
                    subtitle = "健康防病规范 · 饭前便后勤洗手 · 泡泡消灭病菌",
                    tag = "⭐ 即将上线",
                    tagBg = KidAppleGreen,
                    accentColor = KidAppleGreen,
                    icon = Icons.Default.Sanitizer,
                    isAvailable = false,
                    onClick = {
                        soundManager.speak("洗手七步法儿歌很快就来啦！")
                    }
                )
            }

            // 4. Toy Tidying (Manners)
            if (selectedCategory == HabitCategory.ALL || selectedCategory == HabitCategory.MANNERS) {
                HabitGameCard(
                    title = "🧸 玩具要回家",
                    subtitle = "收纳整理规范 · 物归原位 · 自己动手收拾小房间",
                    tag = "⭐ 即将上线",
                    tagBg = KidAmber,
                    accentColor = KidAmber,
                    icon = Icons.Default.Toys,
                    isAvailable = false,
                    onClick = {
                        soundManager.speak("玩完玩具记得放回盒子里哦！")
                    }
                )
            }

            // 5. Clean Plate (Manners)
            if (selectedCategory == HabitCategory.ALL || selectedCategory == HabitCategory.MANNERS) {
                HabitGameCard(
                    title = "🥗 光盘小达人",
                    subtitle = "健康饮食规范 · 营养均衡不挑食 · 珍惜粮食按时吃饭",
                    tag = "⭐ 即将上线",
                    tagBg = KidSoftOrange,
                    accentColor = KidSoftOrange,
                    icon = Icons.Default.Fastfood,
                    isAvailable = false,
                    onClick = {
                        soundManager.speak("不挑食身体棒，一起做光盘小标兵！")
                    }
                )
            }

            // 6. Garbage Sorting (Manners/Safety)
            if (selectedCategory == HabitCategory.ALL || selectedCategory == HabitCategory.MANNERS) {
                HabitGameCard(
                    title = "🗑️ 垃圾分类小能手",
                    subtitle = "公共环保规范 · 垃圾不落地 · 学会认识基础四分类",
                    tag = "⭐ 即将上线",
                    tagBg = KidPurple,
                    accentColor = KidPurple,
                    icon = Icons.Default.DeleteSweep,
                    isAvailable = false,
                    onClick = {
                        soundManager.speak("爱护地球小环境，垃圾分类投进桶！")
                    }
                )
            }
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

    if (showUpdateDialog) {
        UpdateDialog(
            updateManager = updateManager,
            onDismiss = { showUpdateDialog = false }
        )
    }
}

@Composable
fun HabitGameCard(
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
                                accentColor.copy(alpha = if (isAvailable) 1f else 0.45f),
                                accentColor.copy(alpha = if (isAvailable) 0.8f else 0.25f)
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
                    color = if (isAvailable) Color.Gray else Color.LightGray,
                    lineHeight = 16.sp
                )
            }

            // Play / Arrow Icon
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
