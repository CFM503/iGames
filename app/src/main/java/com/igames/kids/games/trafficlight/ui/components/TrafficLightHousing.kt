package com.igames.kids.games.trafficlight.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.igames.kids.core.theme.LampHousingBorder
import com.igames.kids.core.theme.LampHousingDark

@Composable
fun TrafficLightHousing(
    modifier: Modifier = Modifier,
    innerPadding: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(36.dp),
                ambientColor = Color.Black,
                spotColor = Color.Black
            )
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2C343D),
                        LampHousingDark,
                        Color(0xFF14171A)
                    )
                ),
                shape = RoundedCornerShape(36.dp)
            )
            .border(
                width = 4.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        LampHousingBorder,
                        Color(0xFF1B1F23)
                    )
                ),
                shape = RoundedCornerShape(36.dp)
            )
            .padding(innerPadding),
        contentAlignment = Alignment.Center,
        content = content
    )
}
