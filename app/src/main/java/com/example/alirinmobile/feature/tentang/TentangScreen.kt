package com.example.alirinmobile.feature.tentang

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alirinmobile.ui.components.*
import com.example.alirinmobile.ui.theme.*

@Composable
fun TentangScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Bg)) {
        AlirinTopBar(title = "Tentang ALIRIN", onBack = onBack)
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Hero brand block — droplet motif (top-end) lives behind the text.
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(Radius.xl)
                    .background(Primary)
            ) {
                // Decorative outline droplet bleeding off the top-right corner
                HeroDropletMotif(
                    Modifier
                        .size(180.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 50.dp, y = (-50).dp)
                )
                Column(
                    Modifier.padding(start = 22.dp, end = 22.dp, top = 28.dp, bottom = 24.dp)
                ) {
                    Row(
                        Modifier
                            .clip(Radius.pill)
                            .background(Color.White.copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(AlirinIcons.sparkles, null, tint = Color.White, modifier = Modifier.size(12.dp))
                        Text(
                            "SMART CITY BANDAR LAMPUNG",
                            color = Color.White,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.W700,
                            letterSpacing = 1.05.sp,
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "Drainase yang\nkita jaga bareng.",
                        color = Color.White,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.W700,
                        letterSpacing = (-0.75).sp,
                        lineHeight = 32.sp,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "ALIRIN memetakan titik rawan genangan secara preventif — menggabungkan laporan warga, data historis, prakiraan BMKG, dan sensor lokal.",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 13.5.sp,
                        lineHeight = 20.sp,
                    )
                }
            }

            // How it works
            Column {
                SectionHeader(eyebrow = "Cara kerja", title = "Empat sumber, satu peta.")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    HowRow(icon = AlirinIcons.users, tone = HowTone.Indigo,
                        title = "Laporan Warga",
                        body = "Kamu lapor, sistem skor risiko. 3+ laporan di radius 100 m / 24 jam → auto-verifikasi gotong-royong.")
                    HowRow(icon = AlirinIcons.history, tone = HowTone.Default,
                        title = "Data Historis",
                        body = "Titik rawan per kelurahan dari data tahun lalu (kerja sama dengan kelurahan).")
                    HowRow(icon = AlirinIcons.cloud, tone = HowTone.Amber,
                        title = "Cuaca BMKG",
                        body = "Prakiraan hujan per kecamatan ditarik real-time, melahirkan alert preventif.")
                    HowRow(icon = AlirinIcons.sensor, tone = HowTone.Default,
                        title = "Sensor IoT",
                        body = "Placeholder untuk integrasi sensor air lokal (roadmap v2).")
                }
            }

            // Mode contrast
            Column {
                SectionHeader(eyebrow = "Cara lapor", title = "Cepat atau lengkap.")
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ModeRow(
                        color = Primary, icon = AlirinIcons.bolt,
                        title = "Lapor Cepat", sub = "±10 detik · tanpa foto",
                        kvs = listOf(
                            "Field" to "Lokasi + kategori + severity",
                            "Skor bukti" to "multiplier ×0.7",
                            "Cocok untuk" to "lapor cepat saat hujan",
                        ),
                    )
                    ModeRow(
                        color = Amber, icon = AlirinIcons.camera,
                        title = "Lapor Lengkap", sub = "±60s · dengan bukti foto",
                        kvs = listOf(
                            "Field" to "+ deskripsi + 1–3 foto",
                            "Skor bukti" to "multiplier ×1.0",
                            "Cocok untuk" to "masalah serius / butuh tindak cepat",
                        ),
                    )
                }
            }

            // Privacy card
            AlirinCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(18.dp)) {
                Column {
                    Text("Privasi", style = AlirinText.eyebrow, modifier = Modifier.padding(bottom = 8.dp))
                    Text("Anonim per perangkat.", style = AlirinText.h3, modifier = Modifier.padding(bottom = 10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            "Tanpa daftar — identitas otomatis dibuat di perangkatmu.",
                            "Wajah orang lewat otomatis di-blur di foto.",
                            "Nama & kontak hanya minta jika kamu mau dihubungi balik.",
                        ).forEach {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(AlirinIcons.check, null, tint = Primary, modifier = Modifier.size(16.dp))
                                Text(it, fontSize = 13.5.sp, color = Ink2, lineHeight = 20.sp, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            // Stats card (dark)
            AlirinCard(modifier = Modifier.fillMaxWidth(), bg = Ink, padding = PaddingValues(18.dp)) {
                Column {
                    Text(
                        "SEJAK MARET 2026",
                        color = Color.White.copy(alpha = 0.5f),
                        fontWeight = FontWeight.W700,
                        fontSize = 11.sp,
                        letterSpacing = 1.21.sp,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(18.dp), modifier = Modifier.fillMaxWidth()) {
                        StatCol(n = "2.847", l = "Laporan masuk", modifier = Modifier.weight(1f))
                        Box(Modifier.width(1.dp).height(40.dp).background(Color.White.copy(alpha = 0.1f)))
                        StatCol(n = "78%", l = "Selesai", modifier = Modifier.weight(1f))
                        Box(Modifier.width(1.dp).height(40.dp).background(Color.White.copy(alpha = 0.1f)))
                        StatCol(n = "20", l = "Kecamatan", modifier = Modifier.weight(1f))
                    }
                }
            }

            // Link list
            Column {
                TentangLink(icon = AlirinIcons.document, label = "Panduan singkat")
                TentangLink(icon = AlirinIcons.shield,   label = "Kebijakan privasi")
                TentangLink(icon = AlirinIcons.info,     label = "FAQ")
                TentangLink(icon = AlirinIcons.bell,     label = "Versi 1.0 · build 2026.05.19", muted = true)
            }
        }
    }
}

/**
 * Decorative outline droplet for the Tentang hero — mirrors the SVG used in the
 * Claude Design source (two concentric droplet curves, ~18% white over the green bg).
 * The path is drawn against a normalised 180×180 viewBox then scaled to fit.
 */
@Composable
private fun HeroDropletMotif(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val sx = size.width / 180f
        val sy = size.height / 180f
        val stroke = Stroke(width = 1.5.dp.toPx())
        val outerColor = Color.White.copy(alpha = 0.18f)
        val innerColor = Color.White.copy(alpha = 0.126f) // 18% × 0.7 opacity

        val outer = Path().apply {
            moveTo(90f * sx, 10f * sy)
            cubicTo(
                90f * sx, 10f * sy,
                30f * sx, 80f * sy,
                30f * sx, 120f * sy,
            )
            cubicTo(
                30f * sx, 153f * sy,
                57f * sx, 180f * sy,
                90f * sx, 180f * sy,
            )
            cubicTo(
                123f * sx, 180f * sy,
                150f * sx, 153f * sy,
                150f * sx, 120f * sy,
            )
            cubicTo(
                150f * sx, 80f * sy,
                90f * sx, 10f * sy,
                90f * sx, 10f * sy,
            )
            close()
        }
        drawPath(outer, outerColor, style = stroke)

        val inner = Path().apply {
            moveTo(90f * sx, 50f * sy)
            cubicTo(
                90f * sx, 50f * sy,
                50f * sx, 100f * sy,
                50f * sx, 125f * sy,
            )
            cubicTo(
                50f * sx, 145f * sy,
                68f * sx, 165f * sy,
                90f * sx, 165f * sy,
            )
        }
        drawPath(inner, innerColor, style = stroke)
    }
}

private enum class HowTone { Default, Indigo, Amber }

@Composable
private fun HowRow(icon: ImageVector, tone: HowTone, title: String, body: String) {
    val ink = when (tone) {
        HowTone.Indigo  -> Primary
        HowTone.Amber   -> Amber2
        HowTone.Default -> Ink2
    }
    AlirinCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(14.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.Top) {
            Icon(
                icon, null, tint = ink,
                modifier = Modifier.size(24.dp).padding(top = 1.dp),
            )
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.W700, fontSize = 14.sp, color = Ink, modifier = Modifier.padding(bottom = 2.dp))
                Text(body, fontSize = 12.5.sp, color = Ink3, lineHeight = 18.sp)
            }
        }
    }
}

@Composable
private fun ModeRow(color: Color, icon: ImageVector, title: String, sub: String, kvs: List<Pair<String, String>>) {
    AlirinCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(16.dp)) {
        Column {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
                Column {
                    Text(title, fontWeight = FontWeight.W700, fontSize = 14.5.sp, color = Ink)
                    Text(sub, style = AlirinText.caption)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                kvs.forEachIndexed { i, (k, v) ->
                    Column {
                        if (i > 0) Box(Modifier.fillMaxWidth().height(1.dp).background(Hairline))
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(k, color = Muted, fontWeight = FontWeight.W500, fontSize = 12.sp)
                            Text(v, fontWeight = FontWeight.W600, fontSize = 12.sp, color = Ink)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCol(n: String, l: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(n, fontSize = 22.sp, fontWeight = FontWeight.W700, color = Color.White, letterSpacing = (-0.44).sp, lineHeight = 22.sp)
        Text(l, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp, fontWeight = FontWeight.W500, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun TentangLink(icon: ImageVector, label: String, muted: Boolean = false) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable {}
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(icon, null, tint = if (muted) Muted else Ink3, modifier = Modifier.size(18.dp))
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.W500, color = if (muted) Muted else Ink, modifier = Modifier.weight(1f))
        if (!muted) Icon(AlirinIcons.chevronRight, null, tint = Muted, modifier = Modifier.size(16.dp))
    }
}
