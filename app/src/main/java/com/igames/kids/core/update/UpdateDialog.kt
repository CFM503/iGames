package com.igames.kids.core.update

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.igames.kids.core.components.CuteButton
import com.igames.kids.core.theme.KidAppleGreen
import com.igames.kids.core.theme.KidCandyRed
import com.igames.kids.core.theme.KidDeepBlue
import com.igames.kids.core.theme.KidSkyBlue
import com.igames.kids.core.theme.KidSunYellow
import kotlinx.coroutines.launch

@Composable
fun UpdateDialog(
    updateManager: UpdateManager,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val updateInfo by updateManager.updateInfo.collectAsState()
    val downloadStatus by updateManager.downloadStatus.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(10.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Rocket Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(KidSkyBlue.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.RocketLaunch,
                        contentDescription = null,
                        tint = KidSkyBlue,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "发现新版本！",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = KidDeepBlue
                )

                Text(
                    text = "最新版本: v${updateInfo.latestVersionName}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = KidAppleGreen
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Release notes box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF5F7FA))
                        .padding(14.dp)
                ) {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text(
                            text = "💡 更新内容亮点：",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = KidDeepBlue
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = updateInfo.releaseNotes.ifBlank { "包含多项日常规范启蒙小游戏更新与体验优化！" },
                            fontSize = 13.sp,
                            color = Color(0xFF455A64),
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Download Status / Action Buttons
                when (val status = downloadStatus) {
                    is DownloadStatus.Idle -> {
                        CuteButton(
                            onClick = {
                                coroutineScope.launch {
                                    updateManager.downloadApk(updateInfo.downloadUrl, updateInfo.apkFileName)
                                }
                            },
                            backgroundColor = KidAppleGreen,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("立即下载更新", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }
                    is DownloadStatus.Downloading -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            LinearProgressIndicator(
                                progress = { status.progress / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = KidAppleGreen,
                                trackColor = KidAppleGreen.copy(alpha = 0.2f),
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "正在下载中… ${status.progress}%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = KidDeepBlue
                            )
                        }
                    }
                    is DownloadStatus.Completed -> {
                        CuteButton(
                            onClick = {
                                updateManager.installApk(status.apkPath)
                            },
                            backgroundColor = KidSkyBlue,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.DownloadDone, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("下载完成，立即安装", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }
                    is DownloadStatus.Failed -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.ErrorOutline, contentDescription = null, tint = KidCandyRed, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = status.reason, fontSize = 13.sp, color = KidCandyRed)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            CuteButton(
                                onClick = {
                                    coroutineScope.launch {
                                        updateManager.downloadApk(updateInfo.downloadUrl, updateInfo.apkFileName)
                                    }
                                },
                                backgroundColor = KidCandyRed,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("重试下载", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Dismiss button
                Text(
                    text = "稍后再说",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onDismiss() }
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }
    }
}
