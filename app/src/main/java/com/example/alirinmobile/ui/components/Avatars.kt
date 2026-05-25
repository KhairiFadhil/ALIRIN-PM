package com.example.alirinmobile.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alirinmobile.ui.theme.Primary
import com.example.alirinmobile.ui.theme.PrimaryOnDark
import com.example.alirinmobile.ui.theme.Surface

private val AvColors = listOf(
    Color(0xFFE0F0E5) to Color(0xFF1F5A37),
    Color(0xFFFBE7C2) to Color(0xFF6B4502),
    Color(0xFFF7CFC9) to Color(0xFF6B1812),
    Color(0xFFE0EDF8) to Color(0xFF1B4377),
    Color(0xFFEBE2F5) to Color(0xFF41246B),
    Color(0xFFE7E2D9) to Color(0xFF3A3022),
)

private val DefaultLabels = listOf("BL", "DI", "RS", "AP", "TM", "KR")

@Composable
fun Avatar(
    seed: Int = 0,
    size: Int = 28,
    label: String? = null,
    badge: Boolean = false,
    ringColor: Color = Surface,
    modifier: Modifier = Modifier,
) {
    val (bg, ink) = AvColors[seed % AvColors.size]
    val text = label ?: DefaultLabels[seed % DefaultLabels.size]
    Box(modifier.size(size.dp)) {
        Box(
            Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(bg),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                color = ink,
                fontWeight = FontWeight.W700,
                fontSize = (size * 0.42f).sp,
            )
        }
        if (badge) {
            Box(
                Modifier
                    .size(10.dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(Primary)
                    .border(2.dp, ringColor, CircleShape)
            )
        }
    }
}

@Composable
fun AvatarStack(
    count: Int = 3,
    size: Int = 24,
    dark: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val ringColor = if (dark) Primary else Surface
    val overlap = 8.dp
    val totalWidth = (size - 8) * (count - 1) + size
    Box(modifier.width(totalWidth.dp).height(size.dp)) {
        repeat(count) { i ->
            Box(
                Modifier
                    .size(size.dp)
                    .offset(x = (i * (size - 8)).dp)
                    .clip(CircleShape)
                    .background(ringColor)
                    .padding(2.dp),
            ) {
                Avatar(seed = i + 1, size = size - 4, ringColor = ringColor)
            }
        }
    }
}

/** Square logo badge (the "W"-style box in design). */
@Composable
fun LogoBadge(size: Int = 44, radius: Int = 12, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(size.dp)
            .clip(RoundedCornerShape(radius.dp))
            .background(Primary),
        contentAlignment = Alignment.Center,
    ) {
        val droplet = (size * 0.55f).dp
        Canvas(Modifier.size(droplet)) {
            val w = this.size.width
            val h = this.size.height
            // Outer droplet shape (white)
            val path = Path().apply {
                moveTo(w * 0.5f, h * 0.125f)
                cubicTo(
                    w * 0.5f, h * 0.125f,
                    w * 0.208f, h * 0.417f,
                    w * 0.208f, h * 0.625f,
                )
                cubicTo(
                    w * 0.208f, h * 0.792f,
                    w * 0.333f, h * 0.917f,
                    w * 0.5f,  h * 0.917f,
                )
                cubicTo(
                    w * 0.667f, h * 0.917f,
                    w * 0.792f, h * 0.792f,
                    w * 0.792f, h * 0.625f,
                )
                cubicTo(
                    w * 0.792f, h * 0.417f,
                    w * 0.5f,   h * 0.125f,
                    w * 0.5f,   h * 0.125f,
                )
                close()
            }
            drawPath(path, color = Color.White)
            // Highlight stroke
            val highlight = Path().apply {
                moveTo(w * 0.5f, h * 0.125f)
                cubicTo(
                    w * 0.5f, h * 0.125f,
                    w * 0.208f, h * 0.417f,
                    w * 0.208f, h * 0.625f,
                )
            }
            drawPath(
                highlight,
                brush = SolidColor(PrimaryOnDark),
                style = Stroke(width = 2.dp.toPx())
            )
            // Touch unused import warning
            Offset.Zero
        }
    }
}
