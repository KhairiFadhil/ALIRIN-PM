package com.example.alirinmobile.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alirinmobile.ui.theme.AlirinText
import com.example.alirinmobile.ui.theme.PrimarySofter
import com.example.alirinmobile.ui.theme.Surface2

enum class PlaceholderTone { Neutral, Warm, Indigo }

@Composable
fun AlirinPlaceholder(
    label: String,
    modifier: Modifier = Modifier,
    height: Int? = 160,
    tone: PlaceholderTone = PlaceholderTone.Neutral,
    shape: RoundedCornerShape = RoundedCornerShape(18.dp),
) {
    val baseBg = when (tone) {
        PlaceholderTone.Warm -> Color(0xFFFBEEDA)
        PlaceholderTone.Indigo -> PrimarySofter
        PlaceholderTone.Neutral -> Surface2
    }
    val stripeColor = when (tone) {
        PlaceholderTone.Warm -> Color(0x1AD97706)
        PlaceholderTone.Indigo -> Color(0x1A1F5A37)
        PlaceholderTone.Neutral -> Color(0x0A141C1C)
    }
    Box(
        modifier
            .then(if (height != null) Modifier.height(height.dp).fillMaxWidth() else Modifier.fillMaxSize())
            .clip(shape)
            .background(baseBg)
    ) {
        Canvas(Modifier.matchParentSize()) {
            val w = size.width; val h = size.height
            val step = 14.dp.toPx()
            val stroke = 6.dp.toPx()

            var x = -h
            while (x < w + h) {
                drawLine(
                    color = stripeColor,
                    start = Offset(x, 0f),
                    end = Offset(x + h, h),
                    strokeWidth = stroke,
                    pathEffect = PathEffect.cornerPathEffect(0f),
                )
                x += step
            }
        }
        Text(
            text = "[ $label ]",
            modifier = Modifier.align(Alignment.Center),
            style = AlirinText.mono.copy(
                fontSize = 10.sp,
                color = Color(0x80141C1C),
            )
        )
    }
}
