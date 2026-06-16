package com.example.alirinmobile.feature.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alirinmobile.data.Report
import com.example.alirinmobile.data.ReportStatus
import com.example.alirinmobile.ui.components.*
import com.example.alirinmobile.ui.theme.*

@Composable
fun StaffTindakLanjutScreen(
    reports: List<Report>,
    onClose: (Report) -> Unit = {},
) {
    var tab by rememberSaveable { mutableStateOf("berjalan") }

    val berjalan = reports.filter { it.status == ReportStatus.Scheduled || it.status == ReportStatus.InProgress }
    val selesai  = reports.filter { it.status == ReportStatus.Completed }
    val visible = if (tab == "berjalan") berjalan else selesai

    Column(Modifier.fillMaxSize().background(Bg)) {
        AlirinTopBar(
            title = "Tindak Lanjut",
            subtitle = "${berjalan.size} berjalan · ${selesai.size} selesai",
        )

        // Segmented tab control
        Row(
            Modifier
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .fillMaxWidth()
                .clip(Radius.md)
                .background(Surface2)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            SegBtn(
                label = "Sedang berjalan · ${berjalan.size}",
                active = tab == "berjalan",
                onClick = { tab = "berjalan" },
                modifier = Modifier.weight(1f),
            )
            SegBtn(
                label = "Selesai · ${selesai.size}",
                active = tab == "selesai",
                onClick = { tab = "selesai" },
                modifier = Modifier.weight(1f),
            )
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(visible) { TindakLanjutItem(it, onClose = { onClose(it) }) }
            if (visible.isEmpty()) {
                item {
                    Box(
                        Modifier.fillMaxWidth().padding(top = 48.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(AlirinIcons.check, null, tint = Primary, modifier = Modifier.size(36.dp))
                            Spacer(Modifier.height(8.dp))
                            Text(
                                if (tab == "berjalan") "Tidak ada pekerjaan aktif." else "Belum ada yang selesai bulan ini.",
                                style = AlirinText.caption,
                            )
                        }
                    }
                }
            }
            item {
                Spacer(Modifier.height(16.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(Radius.md)
                        .background(Surface2)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(AlirinIcons.info, null, tint = Muted, modifier = Modifier.size(18.dp).padding(top = 1.dp))
                    Text(
                        "Setelah selesai di lokasi, tim akan upload foto \"after\" untuk menutup laporan.",
                        style = AlirinText.caption,
                        lineHeight = 18.sp,
                    )
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SegBtn(label: String, active: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(Radius.sm)
            .background(if (active) Surface else Color.Transparent)
            .then(if (active) Modifier.shadow(1.dp, Radius.sm) else Modifier)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            label,
            color = if (active) Ink else Muted,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.W600,
            maxLines = 1,
        )
    }
}

@Composable
private fun TindakLanjutItem(report: Report, onClose: () -> Unit) {
    val teamName = listOf("Tim Kebersihan A", "Tim Drainase B", "Tim PUPR C")[
        (report.id.hashCode() % 3 + 3) % 3
    ]
    val seed = (report.id.hashCode() % 6 + 6) % 6
    val scheduledFor = when (report.status) {
        ReportStatus.Scheduled -> "Hari ini · sore"
        ReportStatus.InProgress -> "Sedang berjalan"
        ReportStatus.Completed -> "Selesai · ${report.updatedAt ?: report.createdAt}"
        else -> report.updatedAt ?: report.createdAt
    }
    AlirinCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(horizontal = 18.dp, vertical = 16.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(report.code, style = AlirinText.monoCode.copy(fontSize = 11.5.sp))
                    Text(
                        report.category,
                        fontWeight = FontWeight.W700,
                        fontSize = 15.sp,
                        color = Ink,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Text("${report.kelurahan}, ${report.kecamatan}", style = AlirinText.caption)
                }
                StatusPill(status = report.status)
            }
            // Team row
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(Radius.sm)
                    .background(Surface2)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Avatar(seed = seed, size = 28, label = teamName.takeLast(1))
                Column(Modifier.weight(1f)) {
                    Text(teamName, fontWeight = FontWeight.W600, fontSize = 13.sp, color = Ink)
                    Text(scheduledFor, style = AlirinText.caption, modifier = Modifier.padding(top = 1.dp))
                }
                if (report.status != ReportStatus.Completed) {
                    Box(
                        Modifier
                            .clip(Radius.pill)
                            .background(Surface)
                            .border(1.dp, Hairline, Radius.pill)
                            .clickable(onClick = onClose)
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                    ) {
                        Text("Tutup", color = Ink, fontWeight = FontWeight.W600, fontSize = 12.5.sp)
                    }
                }
            }
        }
    }
}
