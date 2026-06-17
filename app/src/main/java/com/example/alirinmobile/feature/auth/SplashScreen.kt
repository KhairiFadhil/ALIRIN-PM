package com.example.alirinmobile.feature.auth

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alirinmobile.ui.components.LogoBadge
import com.example.alirinmobile.ui.theme.Primary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onDone: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1800)
        onDone()
    }
    Box(
        Modifier
            .fillMaxSize()
            .background(Primary),
        contentAlignment = Alignment.Center,
    ) {

        Canvas(Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f - 60.dp.toPx()
            val ringColor = Color.White.copy(alpha = 0.18f)
            val dash = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 8.dp.toPx()), 0f)
            listOf(280.dp, 200.dp, 120.dp).forEach { r ->
                drawCircle(
                    color = ringColor,
                    center = Offset(cx, cy),
                    radius = r.toPx(),
                    style = Stroke(width = 0.8.dp.toPx(), pathEffect = dash),
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {

            Box(
                Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color.White.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center,
            ) {
                LogoBadge(size = 64, radius = 18)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "ALIRIN",
                    color = Color.White,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.W800,
                    letterSpacing = (-0.85).sp,
                )
                Text(
                    "SMART CITY · BANDAR LAMPUNG",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.W600,
                    letterSpacing = 1.4.sp,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.padding(top = 8.dp)) {
                repeat(3) { i -> PulseDot(delayMs = i * 180) }
            }
        }

        Text(
            "Bersama jaga drainase kota",
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 11.sp,
            fontWeight = FontWeight.W500,
            letterSpacing = 0.5.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp),
        )
    }
}

@Composable
private fun PulseDot(delayMs: Int) {
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0.3f,
        animationSpec = tween(700),
        label = "pulse",
    )
    LaunchedEffect(Unit) {
        delay(delayMs.toLong())
        while (true) {
            visible = !visible
            delay(700)
        }
    }
    Box(
        Modifier
            .size(6.dp)
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = alpha))
    )
}
