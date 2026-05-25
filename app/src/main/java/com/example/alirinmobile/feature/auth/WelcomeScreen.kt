package com.example.alirinmobile.feature.auth

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alirinmobile.ui.components.AlirinButton
import com.example.alirinmobile.ui.components.AlirinIcons
import com.example.alirinmobile.ui.components.LogoBadge
import com.example.alirinmobile.ui.theme.Bg
import com.example.alirinmobile.ui.theme.Ink2
import com.example.alirinmobile.ui.theme.Muted
import com.example.alirinmobile.ui.theme.Primary

@Composable
fun WelcomeScreen(
    onWarga: () -> Unit,
    onStaff: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Bg)
    ) {
        // Hero — green block taking most of the screen
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Primary),
        ) {
            // Concentric dashed rings background
            Canvas(Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f - 80.dp.toPx()
                val ringColor = Color.White.copy(alpha = 0.16f)
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
            // Logo top-left
            Row(
                Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 24.dp, top = 60.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                LogoBadge(size = 28, radius = 8)
                Text(
                    "ALIRIN",
                    color = Color.White,
                    fontWeight = FontWeight.W800,
                    fontSize = 16.sp,
                    letterSpacing = (-0.16).sp,
                )
            }
            // Headline bottom-left
            Column(
                Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 28.dp, end = 28.dp, bottom = 36.dp),
            ) {
                Text(
                    "Drainase yang\nkita jaga bareng.",
                    color = Color.White,
                    fontWeight = FontWeight.W700,
                    fontSize = 32.sp,
                    lineHeight = 34.sp,
                    letterSpacing = (-0.8).sp,
                    modifier = Modifier.padding(bottom = 10.dp),
                )
                Text(
                    "Lapor titik genangan di sekitarmu dalam 10 detik. Tanpa daftar, anonim.",
                    color = Color.White.copy(alpha = 0.72f),
                    fontSize = 13.5.sp,
                    lineHeight = 20.sp,
                )
            }
        }

        // CTA area
        Column(
            Modifier
                .fillMaxWidth()
                .background(Bg)
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            AlirinButton(
                label = "Mulai sebagai Warga",
                onClick = onWarga,
                block = true,
                trailingIcon = AlirinIcons.arrowRight,
            )
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Petugas?", color = Muted, fontSize = 12.5.sp)
                Text(
                    "  Masuk sebagai Staff",
                    color = Ink2,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.W600,
                    style = TextStyle(textDecoration = TextDecoration.Underline),
                    modifier = Modifier.clickable(onClick = onStaff),
                )
            }
        }
    }
}
