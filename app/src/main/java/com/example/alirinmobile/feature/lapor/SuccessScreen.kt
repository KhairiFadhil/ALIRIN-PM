package com.example.alirinmobile.feature.lapor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.alirinmobile.data.Kategoris
import com.example.alirinmobile.data.ReportMode
import com.example.alirinmobile.data.RiskLevel
import com.example.alirinmobile.data.Severities
import com.example.alirinmobile.feature.peta.MapBackground
import com.example.alirinmobile.feature.peta.MapMarker
import com.example.alirinmobile.feature.peta.MapStyle
import com.example.alirinmobile.ui.components.*
import com.example.alirinmobile.ui.theme.*

@Composable
fun SuccessScreen(
    code: String,
    mode: ReportMode,
    form: LaporForm,
    onClose: () -> Unit,
    onGoToStatus: () -> Unit,
) {
    val cat = Kategoris.find { it.id == form.kategori } ?: Kategoris[0]
    val sev = Severities.find { it.id == form.severity } ?: Severities[1]
    val risk = when (sev.id) {
        "kritis" -> RiskLevel.Kritis
        "parah"  -> RiskLevel.Tinggi
        "sedang" -> RiskLevel.Waspada
        else     -> RiskLevel.Normal
    }

    Column(Modifier.fillMaxSize().background(Bg)) {
        // Header
        Row(
            Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Spacer(Modifier.width(40.dp))
            Text(
                "Laporan terkirim  🎉",
                fontWeight = FontWeight.W600, fontSize = 15.sp, color = Ink,
            )
            AlirinIconBubble(icon = AlirinIcons.close, onClick = onClose, bg = Surface2)
        }

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 16.dp)
        ) {
            Row(
                Modifier.padding(bottom = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                LogoBadge(size = 56, radius = 14)
                Column(Modifier.weight(1f)) {
                    Text(
                        cat.label,
                        fontSize = 22.sp, fontWeight = FontWeight.W700, color = Ink,
                        letterSpacing = (-0.4).sp,
                    )
                    Text(
                        if (mode == ReportMode.Cepat) "Lapor Cepat · Anonim" else "Lapor Lengkap · Anonim",
                        style = AlirinText.caption,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }

            // Top divider above key-value row
            Box(Modifier.fillMaxWidth().height(1.dp).background(Hairline))
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp, bottom = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                KvCol(label = "Nomor", value = code, mono = true, modifier = Modifier.weight(1f))
                KvCol(label = "Severity", value = sev.label, modifier = Modifier.weight(1f))
                KvCol(label = "Lokasi", value = form.kelurahan.ifBlank { "—" }, align = Alignment.End, modifier = Modifier.weight(1f))
            }

            // Mini map
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(Radius.md)
                    .shadow(2.dp, Radius.md)
            ) {
                MapBackground(style = MapStyle.Light)
                Box(Modifier.align(Alignment.Center)) {
                    MapMarker(risk = risk, count = 1)
                }
                Row(
                    Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .clip(Radius.pill)
                        .background(Surface)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(form.kelurahan.ifBlank { "Lokasi" }, fontSize = 11.sp, fontWeight = FontWeight.W600, color = Ink)
                }
                Row(
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .clip(Radius.pill)
                        .background(Surface)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(form.kecamatan.ifBlank { "Bandar Lampung" }, fontSize = 11.sp, fontWeight = FontWeight.W600, color = Ink)
                }
            }
            Spacer(Modifier.height(18.dp))

            // Stakeholders
            Text("Pihak terlibat · 3", style = AlirinText.caption, modifier = Modifier.padding(bottom = 10.dp))
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                StakeholderRow(seed = 0, label = "BL", name = "Kamu", role = "Pelapor", status = "Baru saja", statusPrimary = true)
                StakeholderRow(seed = 1, label = "AD", name = "Admin Kemiling", role = "Validasi", status = "Menunggu verifikasi")
                StakeholderRow(seed = 2, label = "TK", name = "Tim Kebersihan", role = "Tindak lanjut", status = "Belum dijadwalkan")
            }
            Spacer(Modifier.height(14.dp))

            // Estimate
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(Radius.md)
                    .background(PrimarySofter)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(AlirinIcons.clock, null, tint = PrimaryInk, modifier = Modifier.size(18.dp))
                Text(
                    "Status akan diperbarui otomatis dalam 1–6 jam.",
                    color = PrimaryInk,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.W500,
                )
            }
        }

        Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 20.dp)) {
            AlirinButton(label = "Lihat status laporan", onClick = onGoToStatus, block = true)
        }
    }
}

@Composable
private fun KvCol(
    label: String,
    value: String,
    mono: Boolean = false,
    align: Alignment.Horizontal = Alignment.Start,
    modifier: Modifier = Modifier,
) {
    Column(modifier, horizontalAlignment = align) {
        Text(label, style = AlirinText.caption, modifier = Modifier.padding(bottom = 4.dp))
        Text(
            value,
            fontWeight = FontWeight.W700,
            fontSize = 14.sp,
            color = Ink,
            style = if (mono) AlirinText.mono.copy(fontWeight = FontWeight.W600, fontSize = 13.sp, color = Ink) else AlirinText.body.copy(fontWeight = FontWeight.W700),
            letterSpacing = (-0.14).sp,
        )
    }
}

@Composable
private fun StakeholderRow(
    seed: Int,
    label: String,
    name: String,
    role: String,
    status: String,
    statusPrimary: Boolean = false,
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Avatar(seed = seed, size = 36, label = label)
        Column(Modifier.weight(1f)) {
            Text(name, fontWeight = FontWeight.W600, fontSize = 14.5.sp, color = Ink, letterSpacing = (-0.06).sp)
            Text(role, style = AlirinText.caption, modifier = Modifier.padding(top = 1.dp))
        }
        Text(
            status,
            fontSize = 12.sp, fontWeight = FontWeight.W500,
            color = if (statusPrimary) Primary else Muted,
        )
    }
}
