package com.example.alirinmobile.feature.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.alirinmobile.data.RiskLevel
import com.example.alirinmobile.data.auth.AuthSession
import com.example.alirinmobile.ui.components.*
import com.example.alirinmobile.ui.theme.*

private enum class StaffFilter(val id: String, val labelOf: (Int) -> String, val match: (Report) -> Boolean) {
    SEMUA   ("semua",   { "Semua · $it" },         { true }),
    KRITIS  ("kritis",  { "Kritis · $it" },        { it -> it.risk == RiskLevel.Kritis }),
    LENGKAP ("lengkap", { "Dengan foto · $it" },   { it -> it.mode == ReportMode.Lengkap }),
    CEPAT   ("cepat",   { "Tanpa foto · $it" },    { it -> it.mode == ReportMode.Cepat }),
}

@Composable
fun PersetujuanListScreen(
    reports: List<Report>,
    actor: AuthSession?,
    onReportClick: (Report) -> Unit,
    onLogout: () -> Unit,
) {
    val pending = reports.filter { it.status == ReportStatus.Pending }
    val kritis = pending.count { it.risk == RiskLevel.Kritis }
    val processedThisWeek = reports.count { it.status != ReportStatus.Pending }
    val capacity = 12
    val filledSegments = (pending.size.coerceAtMost(capacity))

    var filter by remember { mutableStateOf(StaffFilter.SEMUA) }
    val visible = pending.filter(filter.match)

    Column(Modifier.fillMaxSize().background(Bg)) {
        // Identity row
        Row(
            Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Avatar(seed = (actor?.userId ?: 3) % 6, size = 42, label = (actor?.displayName?.take(2) ?: "ST").uppercase())
            Column(Modifier.weight(1f)) {
                Text("Staff Validator", style = AlirinText.caption)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        actor?.displayName ?: "Staff",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W700,
                        color = Ink,
                        letterSpacing = (-0.24).sp,
                    )
                    Text(
                        "  · Kemiling",
                        color = Muted,
                        fontWeight = FontWeight.W500,
                        fontSize = 13.sp,
                    )
                }
            }
            // Logout (bell icon — reuse for now; tap = logout)
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Surface2)
                    .clickable(onClick = onLogout),
                contentAlignment = Alignment.Center,
            ) {
                Icon(AlirinIcons.bell, null, tint = Ink, modifier = Modifier.size(20.dp))
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Stats hero
            item {
                AlirinGreenCard(
                    modifier = Modifier.fillMaxWidth(),
                    padding = PaddingValues(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 14.dp),
                ) {
                    Column {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top,
                        ) {
                            PillOnDark("Hari ini")
                            // Kritis pill — danger-tinted on dark
                            Row(
                                Modifier
                                    .clip(Radius.pill)
                                    .background(Color(0x40C0332A))
                                    .border(1.dp, Color.White.copy(alpha = 0.15f), Radius.pill)
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Dot(color = Color(0xFFFF8A7E))
                                Text("$kritis kritis", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.W600)
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        Text(
                            "${pending.size} laporan\nmenunggu validasi",
                            color = Color.White,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.W700,
                            letterSpacing = (-0.65).sp,
                            lineHeight = 28.sp,
                        )
                        Spacer(Modifier.height(14.dp))
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            Column {
                                Text("Diproses", color = Color.White.copy(alpha = 0.65f), fontSize = 11.5.sp, fontWeight = FontWeight.W500)
                                Text("$processedThisWeek minggu ini", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.W700)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Rata-rata respon", color = Color.White.copy(alpha = 0.65f), fontSize = 11.5.sp, fontWeight = FontWeight.W500)
                                Text("2.4 jam", color = PrimaryOnDark, fontSize = 18.sp, fontWeight = FontWeight.W700)
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        SegmentedProgress(filled = filledSegments, total = capacity)
                    }
                }
            }
            // Filter chips
            item {
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(StaffFilter.entries.toList()) { f ->
                        val count = pending.count(f.match)
                        val active = f == filter
                        Box(
                            Modifier
                                .clip(Radius.pill)
                                .background(if (active) Ink else Surface)
                                .then(if (!active) Modifier.border(1.dp, Hairline, Radius.pill) else Modifier)
                                .clickable { filter = f }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                f.labelOf(count),
                                color = if (active) Color.White else Ink3,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.W600,
                            )
                        }
                    }
                }
            }
            // Section header
            item {
                SectionHeader(
                    eyebrow = "Inbox validasi",
                    title = if (visible.isEmpty()) "Tidak ada laporan" else "Perlu kamu cek",
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            // Inbox cards
            items(visible) { r -> InboxItem(report = r, onTap = { onReportClick(r) }) }

            if (visible.isEmpty()) {
                item {
                    Box(
                        Modifier.fillMaxWidth().padding(top = 48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(AlirinIcons.check, null, tint = Primary, modifier = Modifier.size(40.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Semua laporan filter ini sudah tertangani.", style = AlirinText.caption)
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun InboxItem(report: Report, onTap: () -> Unit) {
    val isKritis = report.risk == RiskLevel.Kritis
    Box(
        Modifier
            .fillMaxWidth()
            .clip(Radius.lg)
            .background(Surface)
            .border(
                width = if (isKritis) 1.5.dp else 1.dp,
                color = if (isKritis) RiskKritisDot else Hairline,
                shape = Radius.lg,
            )
            .clickable(onClick = onTap)
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "${report.code} · ${report.createdAt}",
                        style = AlirinText.monoCode.copy(fontSize = 11.5.sp),
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                    Text(
                        report.category,
                        fontWeight = FontWeight.W700,
                        fontSize = 15.5.sp,
                        color = Ink,
                        letterSpacing = (-0.23).sp,
                    )
                    Text(
                        "${report.kelurahan}, ${report.kecamatan}",
                        style = AlirinText.caption,
                        modifier = Modifier.padding(top = 3.dp),
                    )
                }
                RiskPill(level = report.risk, score = report.score)
            }
            if (report.description.isNotBlank()) {
                Text(
                    text = "\"${report.description}\"",
                    color = Ink3,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(Radius.sm)
                        .background(Surface2)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                )
            }
            Row(
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
                    Pill(
                        label = report.photos.toString(),
                        bg = Surface2,
                        ink = Ink3,
                        leadingIcon = AlirinIcons.image,
                    )
                }
                Box(Modifier.weight(1f))
                Icon(AlirinIcons.chevronRight, null, tint = Muted, modifier = Modifier.size(18.dp))
            }
        }
    }
}
