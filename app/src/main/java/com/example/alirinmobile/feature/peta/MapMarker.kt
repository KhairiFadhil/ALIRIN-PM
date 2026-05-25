package com.example.alirinmobile.feature.peta

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alirinmobile.data.RiskLevel
import com.example.alirinmobile.ui.components.AlirinIcons

/**
 * Pin-style marker: white circle, colored ring, colored fill inner circle
 * containing either count or droplet icon.
 */
@Composable
fun MapMarker(
    risk: RiskLevel,
    count: Int?,
    active: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val outerSize = if (active) 50.dp else 44.dp
    Box(
        modifier
            .size(outerSize)
            .shadow(elevation = 6.dp, shape = CircleShape, clip = false)
            .clip(CircleShape)
            .background(Color.White)
            .border(3.dp, risk.dot, CircleShape)
            .padding(3.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(risk.dot),
            contentAlignment = Alignment.Center,
        ) {
            if (count != null && count > 0) {
                Text(
                    text = count.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.W700,
                    fontSize = 14.sp,
                )
            } else {
                Icon(
                    AlirinIcons.droplet, null,
                    tint = Color.White, modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
