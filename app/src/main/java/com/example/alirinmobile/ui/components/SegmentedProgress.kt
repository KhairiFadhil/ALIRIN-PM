package com.example.alirinmobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.alirinmobile.ui.theme.PrimaryOnDark

@Composable
fun SegmentedProgress(
    filled: Int,
    total: Int,
    modifier: Modifier = Modifier,
    onColor: Color = PrimaryOnDark,
    offColor: Color = Color.White.copy(alpha = 0.18f),
    height: Int = 8,
) {
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        repeat(total) { i ->
            Box(
                Modifier
                    .weight(1f)
                    .height(height.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (i < filled) onColor else offColor)
            )
        }
    }
}
