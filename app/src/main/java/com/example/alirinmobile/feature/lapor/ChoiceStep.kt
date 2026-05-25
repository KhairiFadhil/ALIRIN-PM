package com.example.alirinmobile.feature.lapor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alirinmobile.data.ReportMode
import com.example.alirinmobile.ui.components.*
import com.example.alirinmobile.ui.theme.*

@Composable
fun ChoiceStep(onPick: (ReportMode) -> Unit, onBack: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Bg)
    ) {
        AlirinTopBar(onBack = onBack)
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 4.dp)
        ) {
            Text("Langkah 1 dari 3", style = AlirinText.eyebrow, modifier = Modifier.padding(bottom = 6.dp))
            Text("Pilih cara lapor.", style = AlirinText.display, modifier = Modifier.padding(bottom = 8.dp))
            Text(
                "Dua-duanya valid. Bedanya tinggal di kelengkapan bukti dan bobot skor risiko.",
                style = AlirinText.bodyR,
                modifier = Modifier.padding(bottom = 24.dp),
            )
            ModeCard(
                mode = ReportMode.Cepat,
                badge = "±10 detik",
                title = "Lapor Cepat",
                sub = "Tanpa foto",
                body = "Lokasi, kategori, severity. Cukup itu. Jika 3+ warga lapor di area yang sama, sistem auto-verifikasi gotong-royong.",
                highlights = listOf(
                    AlirinIcons.bolt to "Tanpa foto, sangat cepat",
                    AlirinIcons.users to "Gotong-royong: 3 lapor di radius 100 m → otomatis diverifikasi",
                ),
                onPick = { onPick(ReportMode.Cepat) },
            )
            Spacer(Modifier.height(12.dp))
            ModeCard(
                mode = ReportMode.Lengkap,
                badge = "±60–90 detik",
                title = "Lapor Lengkap",
                sub = "Dengan bukti foto",
                badge2 = "Skor lebih tinggi",
                body = "Tambah 1–3 foto kamera (auto-watermark lokasi + waktu). Deskripsi singkat & kontak opsional.",
                highlights = listOf(
                    AlirinIcons.camera to "Foto kamera otomatis di-watermark",
                    AlirinIcons.shield to "Bobot bukti foto penuh — admin prioritaskan",
                ),
                onPick = { onPick(ReportMode.Lengkap) },
            )

            Spacer(Modifier.height(20.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(Radius.md)
                    .background(Color(0x14D97706))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(AlirinIcons.info, null, tint = Amber2, modifier = Modifier.size(18.dp))
                Text(
                    "Lapor dari mana saja. Tidak ada login — identitas anonim per perangkat.",
                    color = AmberInk,
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp,
                )
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ModeCard(
    mode: ReportMode,
    badge: String,
    badge2: String? = null,
    title: String,
    sub: String,
    body: String,
    highlights: List<Pair<ImageVector, String>>,
    onPick: () -> Unit,
) {
    val isCepat = mode == ReportMode.Cepat
    val accent = if (isCepat) Primary else Amber
    Box(
        Modifier
            .fillMaxWidth()
            .shadow(2.dp, Radius.xl, ambientColor = Color(0x14141C1C))
            .clip(Radius.xl)
            .background(Surface)
            .border(1.dp, Hairline, Radius.xl)
            .clickable(onClick = onPick)
            .padding(20.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Icon(
                    if (isCepat) AlirinIcons.bolt else AlirinIcons.camera,
                    null,
                    tint = accent,
                    modifier = Modifier.size(28.dp).padding(top = 2.dp),
                )
                Column(Modifier.weight(1f)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Pill(label = badge, bg = Surface2, ink = Ink3, leadingIcon = AlirinIcons.clock)
                        if (badge2 != null) Pill(label = badge2, bg = AmberSoft, ink = AmberInk, leadingIcon = AlirinIcons.sparkles)
                    }
                    Text(
                        title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.W700,
                        color = Ink,
                        letterSpacing = (-0.4).sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Text(sub, style = AlirinText.caption, modifier = Modifier.padding(top = 2.dp))
                }
                Icon(AlirinIcons.chevronRight, null, tint = Muted, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(14.dp))
            Text(body, style = AlirinText.bodyR, modifier = Modifier.padding(bottom = 14.dp))
            highlights.forEach { (icon, t) ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.padding(bottom = 8.dp),
                ) {
                    Icon(
                        icon, null,
                        tint = if (isCepat) Primary else Amber2,
                        modifier = Modifier.size(16.dp).padding(top = 2.dp),
                    )
                    Text(
                        t,
                        fontSize = 13.sp,
                        color = Ink2,
                        lineHeight = 18.sp,
                    )
                }
            }
        }
    }
}
