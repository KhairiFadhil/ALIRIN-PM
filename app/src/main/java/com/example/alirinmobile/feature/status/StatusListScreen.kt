package com.example.alirinmobile.feature.status

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alirinmobile.data.Report
import com.example.alirinmobile.data.ReportMode
import com.example.alirinmobile.data.ReportStatus
import com.example.alirinmobile.data.StatusSteps
import com.example.alirinmobile.data.timelineIndex
import com.example.alirinmobile.ui.components.*
import com.example.alirinmobile.ui.theme.*

@Composable
fun StatusListScreen(
    reports: List<Report>,
    onBack: () -> Unit,
    onReportClick: (Report) -> Unit,
) {
    Column(Modifier.fillMaxSize().background(Bg)) {
        AlirinTopBar(
            title = "Status Laporan",
            subtitle = "${reports.size} laporan tersimpan",
            onBack = onBack,
            right = { AlirinIconBubble(icon = AlirinIcons.search, onClick = {}) },
        )
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                AlirinCard(
                    modifier = Modifier.fillMaxWidth(),
                    padding = PaddingValues(14.dp),
                ) {
                    Column {
                        Text("Cari pakai nomor laporan", style = AlirinText.caption, modifier = Modifier.padding(bottom = 6.dp))
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clip(Radius.md)
                                .background(Surface2)
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(AlirinIcons.document, null, tint = Muted, modifier = Modifier.size(18.dp))
                            Text("ALR-2026-...", style = AlirinText.mono.copy(fontSize = 14.sp, color = Faint), modifier = Modifier.weight(1f))
                            Box(
                                Modifier
                                    .clip(Radius.pill)
                                    .background(Ink)
                                    .clickable {}
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text("Cari", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.W600)
                            }
                        }
                    }
                }
            }
            item {
                // Filter tabs
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)) {
                    listOf("Semua", "Aktif", "Selesai").forEachIndexed { i, label ->
                        val active = i == 0
                        Box(
                            Modifier
                                .clip(Radius.pill)
                                .background(if (active) Ink else Surface)
                                .then(if (!active) Modifier.border(1.dp, Hairline, Radius.pill) else Modifier)
                                .clickable {}
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                label,
                                color = if (active) Color.White else Ink3,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.W600,
                            )
                        }
                    }
                }
            }
            items(reports) { r ->
                ReportListItem(report = r, onClick = { onReportClick(r) })
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun ReportListItem(report: Report, onClick: () -> Unit) {
    val idx = report.status.timelineIndex()
    AlirinCard(
        modifier = Modifier.fillMaxWidth(),
        padding = PaddingValues(16.dp),
        onClick = onClick,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(report.code, style = AlirinText.monoCode, modifier = Modifier.padding(bottom = 4.dp))
                    Text(
                        report.category,
                        fontWeight = FontWeight.W700, fontSize = 15.sp, color = Ink,
                        letterSpacing = (-0.15).sp,
                        modifier = Modifier.padding(bottom = 2.dp),
                    )
                    Text(
                        "${report.kelurahan} · ${report.kecamatan} · ${report.createdAt}",
                        style = AlirinText.caption,
                    )
                }
                StatusPill(status = report.status)
            }
            // mini step bar
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.fillMaxWidth()) {
                StatusSteps.forEachIndexed { i, _ ->
                    val color = when {
                        i > idx -> Hairline2
                        report.status == ReportStatus.Completed -> RiskNormalDot
                        else -> Primary
                    }
                    Box(
                        Modifier
                            .weight(1f)
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(color)
                    )
                }
            }
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Pill(
                    label = if (report.mode == ReportMode.Cepat) "Cepat" else "Lengkap",
                    bg = if (report.mode == ReportMode.Cepat) PrimarySoft else AmberSoft,
                    ink = if (report.mode == ReportMode.Cepat) PrimaryInk else AmberInk,
                    leadingIcon = if (report.mode == ReportMode.Cepat) AlirinIcons.bolt else AlirinIcons.camera,
                )
                if (report.photos > 0) {
                    Pill(label = "${report.photos} foto", bg = Surface2, ink = Ink3, leadingIcon = AlirinIcons.image)
                }
                RiskPill(level = report.risk)
                Box(Modifier.weight(1f))
                Icon(AlirinIcons.chevronRight, null, tint = Muted, modifier = Modifier.size(18.dp))
            }
        }
    }
}
