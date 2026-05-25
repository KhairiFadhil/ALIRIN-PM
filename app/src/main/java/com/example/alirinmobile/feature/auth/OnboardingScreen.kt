package com.example.alirinmobile.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alirinmobile.ui.components.AlirinButton
import com.example.alirinmobile.ui.components.AlirinIcons
import com.example.alirinmobile.ui.theme.*
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val eyebrow: String,
    val title: String,
    val body: String,
    val icon: ImageVector,
    val tint: Color,
    val tintSoft: Color,
)

private val pages = listOf(
    OnboardingPage(
        eyebrow = "LAPOR CEPAT",
        title = "Lapor drainase\ndalam 10 detik.",
        body = "Cukup pin lokasi & kategori. Tidak perlu daftar, tidak perlu foto. Laporan langsung masuk ke staff kelurahan.",
        icon = AlirinIcons.bolt,
        tint = Primary,
        tintSoft = PrimarySoft,
    ),
    OnboardingPage(
        eyebrow = "GOTONG-ROYONG",
        title = "Verifikasi otomatis\ndari warga lain.",
        body = "Kalau 3+ warga lapor di area yang sama dalam 24 jam, sistem otomatis tandai laporan sebagai diverifikasi warga.",
        icon = AlirinIcons.users,
        tint = Amber,
        tintSoft = AmberSoft,
    ),
    OnboardingPage(
        eyebrow = "PREDIKSI BMKG",
        title = "Tahu risiko banjir\n3 jam ke depan.",
        body = "Skor risiko dihitung otomatis dari prakiraan cuaca BMKG + laporan warga + data historis kelurahan kamu.",
        icon = AlirinIcons.sparkles,
        tint = Sky,
        tintSoft = SkySoft,
    ),
)

@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val isLast = pagerState.currentPage == pages.size - 1

    Column(Modifier.fillMaxSize().background(Bg)) {
        // Top: skip
        Row(
            Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Lewati",
                color = Muted,
                fontSize = 13.sp,
                fontWeight = FontWeight.W600,
                modifier = Modifier.clickable(onClick = onDone).padding(8.dp),
            )
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
        ) { index ->
            OnboardingPageContent(page = pages[index])
        }

        // Page indicators
        Row(
            Modifier.fillMaxWidth().padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(pages.size) { i ->
                val active = i == pagerState.currentPage
                Box(
                    Modifier
                        .padding(horizontal = 4.dp)
                        .size(width = if (active) 24.dp else 8.dp, height = 8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (active) Primary else Hairline2)
                )
            }
        }

        // CTA
        Column(Modifier.padding(start = 24.dp, end = 24.dp, bottom = 28.dp)) {
            AlirinButton(
                label = if (isLast) "Mulai pakai ALIRIN" else "Lanjut",
                trailingIcon = AlirinIcons.arrowRight,
                onClick = {
                    if (isLast) onDone()
                    else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                },
                block = true,
            )
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Big illustration: tinted circle + icon
        Box(
            Modifier
                .size(180.dp)
                .clip(CircleShape)
                .background(page.tintSoft),
            contentAlignment = Alignment.Center,
        ) {
            Icon(page.icon, null, tint = page.tint, modifier = Modifier.size(80.dp))
        }
        Spacer(Modifier.height(40.dp))
        Text(page.eyebrow, style = AlirinText.eyebrow, color = page.tint)
        Spacer(Modifier.height(10.dp))
        Text(
            page.title,
            style = AlirinText.display.copy(fontSize = 28.sp, lineHeight = 32.sp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Spacer(Modifier.height(14.dp))
        Text(
            page.body,
            style = AlirinText.bodyR,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}
