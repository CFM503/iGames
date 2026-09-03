package com.igames.kids.core.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.igames.kids.core.theme.KidAmber
import com.igames.kids.core.theme.KidAppleGreen
import com.igames.kids.core.theme.KidCandyRed
import com.igames.kids.core.theme.KidDeepBlue
import com.igames.kids.core.theme.KidSkyBlue
import kotlin.random.Random

@Composable
fun ParentalGateDialog(
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var numA by remember { mutableIntStateOf(Random.nextInt(3, 9)) }
    var numB by remember { mutableIntStateOf(Random.nextInt(1, 8)) }
    val isAddition by remember { mutableStateOf(Random.nextBoolean()) }

    val correctAnswer = if (isAddition) numA + numB else (numA + numB) - numA
    val displayedA = if (isAddition) numA else numA + numB
    val displayedB = if (isAddition) numB else numA
    val opSymbol = if (isAddition) "+" else "-"

    val options = remember(correctAnswer) {
        val list = mutableListOf(correctAnswer)
        while (list.size < 4) {
            val fake = correctAnswer + Random.nextInt(-4, 5)
            if (fake > 0 && !list.contains(fake)) {
                list.add(fake)
            }
        }
        list.shuffled()
    }

    var showError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🔒 家长验证",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = KidDeepBlue
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "请回答算术题以进入设置：",
                    fontSize = 15.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(KidSkyBlue.copy(alpha = 0.15f))
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "$displayedA $opSymbol $displayedB = ?",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = KidDeepBlue
                    )
                }

                if (showError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "答案不对哦，请再试一次",
                        fontSize = 13.sp,
                        color = KidCandyRed
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    options.forEach { opt ->
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(KidSkyBlue)
                                .clickable {
                                    if (opt == correctAnswer) {
                                        onSuccess()
                                    } else {
                                        showError = true
                                        // Refresh problem
                                        numA = Random.nextInt(3, 9)
                                        numB = Random.nextInt(1, 8)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$opt",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "取消",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Gray,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onDismiss() }
                        .padding(8.dp)
                )
            }
        }
    }
}
