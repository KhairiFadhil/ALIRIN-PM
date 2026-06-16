package com.example.alirinmobile.feature.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.alirinmobile.data.ReportStatus
import com.example.alirinmobile.ui.components.*
import com.example.alirinmobile.ui.theme.*

@Composable
fun StaffStatistikScreen(reports: List<Report>) {
    val validated = reports.count {
        it.status == ReportStatus.Verified || it.status == ReportStatus.Scheduled ||
        it.status == ReportStatus.InProgress || it.status == ReportStatus.Completed
    }
    val bars = listOf(2, 3, 4, 3, 5, 6, 4, 7, 5, 4, 6, 5, 3, 4)  // 14d trend (no per-day source yet)

    // Category breakdown computed from the live report list.
    val palette = listOf(Primary, RiskTinggiDot, RiskWaspadaDot, Ink3, RiskKritisDot, Sky)
    val total = reports.size.coerceAtLeast(1)
    val categoryBreakdown = reports
        .groupingBy { it.category }.eachCount()
        .entries.sortedByDescending { it.value }
        .take(palette.size)
        .mapIndexed { i, e ->
            Triple(e.key, e.value, (e.value * 100) / total) to palette[i]
        }

    // Top kelurahan by report volume — also from live data.
    val topKel = reports
        .groupingBy { it.kelurahan }.eachCount()
        .entries.sortedByDescending { it.value }
        .take(4)
        .map { it.key to it.value }

    Column(Modifier.fillMaxSize().background(Bg)) {
        AlirinTopBar(title = "Statistik", subtitle = "Wilayah Kemiling · Mei 2026")
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Hero green card — kamu vs respon
            AlirinGreenCard(
                modifier = Modifier.fillMaxWidth(),
                padding = PaddingValues(20.dp),
            ) {
                Column {
                    Text(
                        "PERFORMA KAMU BULAN INI",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.W700,
                        letterSpacing = 1.21.sp,
                        modifier = Modifier.padding(bottom = 10.dp),
                    )
                    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                validated.toString(),
                                color = Color.White,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.W800,
                                letterSpacing = (-0.9).sp,
                                lineHeight = 36.sp,
                            )
                            Text(
                                "Laporan divalidasi",
                                color = Color.White.copy(alpha = 0.65f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.W500,
                                modifier = Modifier.padding(top = 6.dp),
                            )
                        }
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                            Text(
                                "2.4h",
                                color = PrimaryOnDark,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.W800,
                                letterSpacing = (-0.9).sp,
                                lineHeight = 36.sp,
                            )
                            Text(
                                "Avg waktu respon",
                                color = Color.White.copy(alpha = 0.65f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.W500,
                                modifier = Modifier.padding(top = 6.dp),
                            )
                        }
                    }
                }
            }

            // Bar chart — 14d daily reports
            AlirinCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(16.dp)) {
                Column {
                    Row(
                        Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top,
                    ) {
                        Column {
                            Text("14 HARI TERAKHIR", style = AlirinText.eyebrow, modifier = Modifier.padding(bottom = 4.dp))
                            Text("Laporan harian", fontSize = 18.sp, fontWeight = FontWeight.W700, color = Ink, letterSpacing = (-0.27).sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Rata-rata", style = AlirinText.caption)
                            Text("4.2/hari", fontSize = 16.sp, fontWeight = FontWeight.W700, color = Primary, modifier = Modifier.padding(top = 2.dp))
                        }
                    }
                    // Bars
                    Row(
                        Modifier.fillMaxWidth().height(120.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        val maxV = bars.max()
                        bars.forEachIndexed { i, v ->
                            Box(
                                Modifier
                                    .weight(1f)
                                    .fillMaxHeight(v.toFloat() / maxV.toFloat())
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (i == bars.lastIndex) Primary else PrimarySoft)
                            )
                        }
                    }
                    Row(
                        Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        listOf("1 Mei", "7 Mei", "13 Mei").forEach {
                            Text(it, color = Muted, fontSize = 10.5.sp, fontWeight = FontWeight.W500)
                        }
                    }
                }
            }

            // Category breakdown
            AlirinCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(16.dp)) {
                Column {
                    Text("PECAHAN KATEGORI", style = AlirinText.eyebrow, modifier = Modifier.padding(bottom = 12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        categoryBreakdown.forEach { (data, color) ->
                            val (label, count, pct) = data
                            Column {
                                Row(
                                    Modifier.fillMaxWidth().padding(bottom = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(label, fontWeight = FontWeight.W600, fontSize = 12.5.sp, color = Ink)
                                    Text("$count · $pct%", color = Muted, fontWeight = FontWeight.W500, fontSize = 12.5.sp)
                                }
                                Box(
                                    Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(Surface2)
                                ) {
                                    Box(
                                        Modifier
                                            .fillMaxWidth(pct / 100f)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(color)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Top kelurahan
            AlirinCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(16.dp)) {
                Column {
                    Text("TOP KELURAHAN", style = AlirinText.eyebrow, modifier = Modifier.padding(bottom = 12.dp))
                    topKel.forEachIndexed { i, (name, count) ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(name, fontWeight = FontWeight.W600, fontSize = 13.5.sp, color = Ink)
                            Text(
                                "$count laporan",
                                style = AlirinText.mono.copy(fontSize = 12.sp, color = Primary, fontWeight = FontWeight.W600),
                            )
                        }
                        if (i < topKel.lastIndex) {
                            Box(Modifier.fillMaxWidth().height(1.dp).background(Hairline))
                        }
                    }
                }
            }
        }
    }
}
