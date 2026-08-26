package com.example.alirinmobile.feature.status

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alirinmobile.data.HistoryEntry
import com.example.alirinmobile.data.Report
import com.example.alirinmobile.data.ReportStatus
import com.example.alirinmobile.data.StatusSteps
import com.example.alirinmobile.data.timelineIndex
import com.example.alirinmobile.feature.peta.MapMarker
import com.example.alirinmobile.feature.peta.StaticMapPreview
import com.example.alirinmobile.ui.components.*
import com.example.alirinmobile.ui.theme.*

@Composable
fun StatusDetailScreen(
    report: Report,
    onBack: () -> Unit,
) {
    val ctx = LocalContext.current
    Column(Modifier.fillMaxSize().background(Bg)) {
        AlirinTopBar(
            title = report.code,
            subtitle = report.category,
            onBack = onBack,
            right = {
                AlirinIconBubble(
                    icon = AlirinIcons.share,
                    onClick = {
                        ctx.shareText(
                            "Laporan drainase ALIRIN ${report.code}\n" +
                                "${report.category} · ${report.kelurahan}, ${report.kecamatan}\n" +
                                "Status: ${report.status.label}",
                        )
                    },
                )
            },
        )
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {

            AlirinCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(18.dp)) {
                Column {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 12.dp)) {
                        StatusPill(status = report.status)
                        RiskPill(level = report.risk, score = report.score)
                    }
                    Text(report.kelurahan, style = AlirinText.h2, modifier = Modifier.padding(bottom = 6.dp))
                    Text(
                        "${report.address ?: report.kecamatan} · dilaporkan ${report.createdAt}",
                        style = AlirinText.caption,
                        modifier = Modifier.padding(bottom = 14.dp),
                    )

                    if (report.description.isNotEmpty()) {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .clip(Radius.md)
                                .background(Surface2)
                                .padding(12.dp)
                                .then(Modifier),
                        ) {
                            Text("Deskripsi pelapor", style = AlirinText.caption, modifier = Modifier.padding(bottom = 4.dp))
                            Text(
                                "\"${report.description}\"",
                                fontSize = 14.sp, fontWeight = FontWeight.W500,
                                color = Ink, lineHeight = 20.sp,
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                    }

                    if (report.photos.isNotEmpty()) {
                        Text("Bukti foto (${report.photos.size})", style = AlirinText.caption, modifier = Modifier.padding(bottom = 8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                            repeat(report.photos.size.coerceAtMost(3)) { i ->
                                AlirinPlaceholder(
                                    label = "foto-${i + 1}",
                                    height = 80,
                                    tone = if (i == 0) PlaceholderTone.Indigo else PlaceholderTone.Neutral,
                                    shape = Radius.md,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }
            }

            AlirinCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(18.dp)) {
                RiskBreakdownCard(items = report.riskBreakdown)
            }

            if (report.aiRiskScore != null) {
                AlirinCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(18.dp)) {
                    AiAssessmentCard(report = report)
                }
            }

            // Blok "Diverifikasi Warga" dihapus di sini. Ia bergantung pada
            // catatan riwayat yang mengandung kata "gotong", dan tidak ada satu
            // pun kode di web, mobile, maupun basis data yang pernah menulis
            // catatan itu -- jadi blok ini tidak akan pernah muncul. Isinya juga
            // mengarang: teksnya menyebut "3+ warga" dan avatarnya dipatok tiga
            // tanpa membaca data apa pun.
            //
            // Verifikasi gotong-royong yang benar butuh tiga PELAPOR berbeda,
            // dan itu baru mungkin setelah tiap perangkat punya identitas
            // (rekomendasi P-8).

            AlirinCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(18.dp)) {
                Column {
                    Row(
                        Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Riwayat status", style = AlirinText.h3)
                        Text("${report.history.size}/${StatusSteps.size} step", style = AlirinText.caption)
                    }
                    TimelineVertical(report = report)
                }
            }

            AlirinCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(0.dp)) {
                Column {
                    Box(Modifier.fillMaxWidth().height(140.dp)) {
                        StaticMapPreview(lat = report.lat, lng = report.lng, modifier = Modifier.matchParentSize())
                        Box(Modifier.align(Alignment.Center).offset(y = (-22).dp)) {
                            MapMarker(risk = report.risk, count = 1)
                        }
                    }
                    Row(
                        Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(AlirinIcons.pin, null, tint = Primary, modifier = Modifier.size(18.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                "${report.kelurahan}, ${report.kecamatan}",
                                fontWeight = FontWeight.W600, fontSize = 13.sp, color = Ink,
                            )
                            Text(
                                if (report.lat != null && report.lng != null)
                                    "%.5f, %.5f".format(report.lat, report.lng)
                                else "Lokasi belum tercatat",
                                style = AlirinText.mono.copy(color = Muted),
                            )
                        }
                        Icon(AlirinIcons.chevronRight, null, tint = Muted, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(Radius.md)
                    .background(Surface2)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(AlirinIcons.info, null, tint = Muted, modifier = Modifier.size(18.dp))
                Text(
                    "Status diperbarui otomatis. Kamu tidak perlu refresh.",
                    style = AlirinText.caption,
                    lineHeight = 18.sp,
                )
            }
        }
    }
}

@Composable
private fun TimelineVertical(report: Report) {
    val reached = report.status.timelineIndex()
    Column {
        StatusSteps.forEachIndexed { i, s ->
            val isLast = i == StatusSteps.size - 1
            val hist: HistoryEntry? = report.history.find { it.status == s.status }
            val done = i <= reached
            val current = i == reached
            Row(modifier = Modifier.padding(bottom = if (isLast) 0.dp else 18.dp)) {

                Box(Modifier.width(28.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(if (done) Ink else Surface2)
                                .then(if (current) Modifier.border(3.dp, PrimarySoft, CircleShape) else Modifier),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (done) {
                                Icon(
                                    if (s.status == ReportStatus.Completed) AlirinIcons.check
                                    else AlirinIcons.byName(s.iconName),
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp),
                                )
                            } else {
                                Box(
                                    Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Faint)
                                )
                            }
                        }
                        if (!isLast) {
                            Box(
                                Modifier
                                    .width(2.dp)
                                    .height(40.dp)
                                    .background(if (done) Ink else Hairline2)
                            )
                        }
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f).padding(top = 2.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            s.status.label,
                            fontWeight = FontWeight.W700,
                            fontSize = 14.sp,
                            color = if (done) Ink else Muted,
                        )
                        if (hist?.live == true) {
                            Row(
                                Modifier
                                    .clip(Radius.pill)
                                    .background(Color(0xFFFEE2E2))
                                    .padding(horizontal = 8.dp, vertical = 1.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Dot(color = Color(0xFFDC2626), size = 5)
                                Text("Live", color = Color(0xFF991B1B), fontSize = 10.sp, fontWeight = FontWeight.W600)
                            }
                        }
                    }
                    if (hist?.when_ != null) {
                        Text(hist.when_, style = AlirinText.caption, modifier = Modifier.padding(top = 2.dp, bottom = if (hist.note != null) 4.dp else 0.dp))
                    }
                    if (hist?.note != null) {
                        Text(hist.note, fontSize = 13.sp, color = Ink2, lineHeight = 19.sp)
                    }
                }
            }
        }
    }
}
