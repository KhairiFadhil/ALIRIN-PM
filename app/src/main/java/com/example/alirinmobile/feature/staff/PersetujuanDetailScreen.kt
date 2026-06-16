package com.example.alirinmobile.feature.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alirinmobile.data.Report
import com.example.alirinmobile.data.ReportMode
import com.example.alirinmobile.data.ReportStatus
import com.example.alirinmobile.data.RiskLevel
import com.example.alirinmobile.data.repository.HotspotSeed
import com.example.alirinmobile.data.repository.ReportSeed
import com.example.alirinmobile.feature.lapor.TextField
import com.example.alirinmobile.feature.peta.MapBackground
import com.example.alirinmobile.feature.peta.MapMarker
import com.example.alirinmobile.feature.peta.MapStyle
import com.example.alirinmobile.ui.components.*
import com.example.alirinmobile.ui.theme.*

private enum class SheetKind { Verify, Reject, Schedule }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersetujuanDetailScreen(
    report: Report,
    onBack: () -> Unit,
    onTransition: (ReportStatus, String?) -> Unit,
) {
    var sheet by remember { mutableStateOf<SheetKind?>(null) }
    var actioned by remember { mutableStateOf<SheetKind?>(null) }
    val ctx = androidx.compose.ui.platform.LocalContext.current

    if (actioned != null) {
        StaffActionSuccess(action = actioned!!, report = report, onDone = onBack)
        return
    }

    Box(Modifier.fillMaxSize().background(Bg)) {
        Column(Modifier.fillMaxSize()) {
            AlirinTopBar(
                title = report.code,
                subtitle = report.createdAt,
                onBack = onBack,
                right = {
                    AlirinIconBubble(
                        icon = AlirinIcons.share,
                        onClick = {
                            ctx.shareText(
                                "Laporan ${report.code} · ${report.category}\n" +
                                    "${report.kelurahan}, ${report.kecamatan} · skor ${report.score}",
                            )
                        },
                    )
                },
            )
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, bottom = 110.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                HeroCard(report)
                if (report.description.isNotBlank()) ReporterDescription(report)
                if (report.photos > 0) PhotoStripCard(report)
                ValidationSignalsCard(report)
                MapSnippetCard(report)
                ReporterCard(report)
            }
        }

        // Fixed action bar at bottom
        ActionBar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            onReject = { sheet = SheetKind.Reject },
            onSchedule = { sheet = SheetKind.Schedule },
            onVerify = { sheet = SheetKind.Verify },
        )
    }

    if (sheet != null) {
        ModalBottomSheet(
            onDismissRequest = { sheet = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape = Radius.sheet,
            containerColor = Surface,
            dragHandle = null,
        ) {
            when (sheet) {
                SheetKind.Verify -> VerifySheet(
                    onClose = { sheet = null },
                    onConfirm = { note ->
                        onTransition(ReportStatus.Verified, note)
                        sheet = null
                        actioned = SheetKind.Verify
                    },
                )
                SheetKind.Reject -> RejectSheet(
                    onClose = { sheet = null },
                    onConfirm = { reason ->
                        onTransition(ReportStatus.Rejected, "Ditolak: $reason")
                        sheet = null
                        actioned = SheetKind.Reject
                    },
                )
                SheetKind.Schedule -> ScheduleSheet(
                    onClose = { sheet = null },
                    onConfirm = { team, time ->
                        onTransition(ReportStatus.Scheduled, "Dijadwalkan: $team · $time")
                        sheet = null
                        actioned = SheetKind.Schedule
                    },
                )
                null -> Unit
            }
        }
    }
}

// ── Hero card ──────────────────────────────────────────────────
@Composable
private fun HeroCard(report: Report) {
    AlirinCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(18.dp)) {
        Column {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 12.dp)) {
                RiskPill(level = report.risk, score = report.score)
                Pill(
                    label = if (report.mode == ReportMode.Cepat) "Cepat" else "Lengkap",
                    bg = if (report.mode == ReportMode.Cepat) PrimarySoft else AmberSoft,
                    ink = if (report.mode == ReportMode.Cepat) PrimaryInk else AmberInk,
                    leadingIcon = if (report.mode == ReportMode.Cepat) AlirinIcons.bolt else AlirinIcons.camera,
                )
            }
            Text(report.category, style = AlirinText.h2, modifier = Modifier.padding(bottom = 6.dp))
            Text(
                "${report.address ?: report.kelurahan} · ${report.kelurahan}, ${report.kecamatan}",
                style = AlirinText.caption,
            )
        }
    }
}

@Composable
private fun ReporterDescription(report: Report) {
    AlirinCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(16.dp)) {
        Column {
            Text("Deskripsi pelapor", style = AlirinText.eyebrow, modifier = Modifier.padding(bottom = 8.dp))
            Text(
                "\"${report.description}\"",
                fontSize = 14.sp,
                fontWeight = FontWeight.W500,
                lineHeight = 21.sp,
                color = Ink2,
            )
        }
    }
}

@Composable
private fun PhotoStripCard(report: Report) {
    AlirinCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(16.dp)) {
        Column {
            Row(
                Modifier.fillMaxWidth().padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Bukti foto · ${report.photos}", style = AlirinText.eyebrow)
                Text("Watermarked", color = Primary, fontSize = 11.sp, fontWeight = FontWeight.W600)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                repeat(report.photos.coerceAtMost(3)) { i ->
                    Box(Modifier.weight(1f)) {
                        AlirinPlaceholder(
                            label = "foto-${i + 1}",
                            height = 100,
                            tone = if (i == 0) PlaceholderTone.Indigo else PlaceholderTone.Neutral,
                            shape = Radius.md,
                        )
                        // Watermark badge bottom-right
                        Column(
                            Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color.Black.copy(alpha = 0.7f))
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                        ) {
                            Text("-5.39812", color = Color.White, fontSize = 6.5.sp, style = AlirinText.mono.copy(color = Color.White, fontSize = 6.5.sp))
                            Text("13/05 09:14", color = Color.White, fontSize = 6.5.sp, style = AlirinText.mono.copy(color = Color.White, fontSize = 6.5.sp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ValidationSignalsCard(report: Report) {
    // Approximate signals from existing data
    val reporterCount = ReportSeed.count { it.kelurahan == report.kelurahan }.coerceAtLeast(1)
    val nearbyHistoric = HotspotSeed.count {
        it.kel == report.kelurahan || it.kec == report.kecamatan
    }

    AlirinCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(16.dp)) {
        Column {
            Text("Sinyal validasi", style = AlirinText.eyebrow, modifier = Modifier.padding(bottom = 10.dp))
            SignalRow(
                icon = AlirinIcons.users,
                label = "Laporan dari warga",
                value = "$reporterCount orang",
                note = if (reporterCount >= 3) "Memenuhi syarat gotong-royong (≥3)"
                       else "Belum memenuhi 3 laporan",
            )
            SignalRow(
                icon = AlirinIcons.map,
                label = "Hotspot historis sekitar",
                value = "$nearbyHistoric titik",
                note = if (nearbyHistoric > 0) "Area langganan rawan" else "Bukan area langganan",
            )
            SignalRow(
                icon = AlirinIcons.droplet,
                label = "Skor risiko sistem",
                value = "${report.score}/100",
                note = "Level: ${report.risk.label}",
                last = true,
            )
        }
    }
}

@Composable
private fun SignalRow(
    icon: ImageVector,
    label: String,
    value: String,
    note: String,
    last: Boolean = false,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
            Icon(icon, null, tint = Ink3, modifier = Modifier.size(18.dp).padding(top = 1.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(label, fontWeight = FontWeight.W600, fontSize = 13.sp, color = Ink, modifier = Modifier.weight(1f))
                    Text(value, fontWeight = FontWeight.W700, fontSize = 13.sp, color = Primary)
                }
                Text(note, style = AlirinText.caption, modifier = Modifier.padding(top = 2.dp))
            }
        }
        if (!last) {
            Spacer(Modifier.height(10.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(Hairline))
        }
    }
}

@Composable
private fun MapSnippetCard(report: Report) {
    AlirinCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(0.dp)) {
        Column {
            Box(Modifier.fillMaxWidth().height(160.dp)) {
                MapBackground(style = MapStyle.Light)
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
                        fontWeight = FontWeight.W600, fontSize = 13.5.sp, color = Ink,
                    )
                    Text("-5.39812, 105.26012", style = AlirinText.mono.copy(color = Muted))
                }
                val navCtx = androidx.compose.ui.platform.LocalContext.current
                Text(
                    "Navigasi",
                    color = Primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.W600,
                    modifier = Modifier.clickable {
                        // Open the location in any maps app via a geo: URI.
                        val label = android.net.Uri.encode("${report.kelurahan}, ${report.kecamatan}")
                        val uri = android.net.Uri.parse("geo:-5.39812,105.26012?q=-5.39812,105.26012($label)")
                        runCatching {
                            navCtx.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, uri))
                        }.onFailure { navCtx.toast("Tidak ada aplikasi peta terpasang.") }
                    },
                )
            }
        }
    }
}

@Composable
private fun ReporterCard(report: Report) {
    AlirinCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(16.dp)) {
        Column {
            Text("Pelapor", style = AlirinText.eyebrow, modifier = Modifier.padding(bottom = 10.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Avatar(seed = (report.id.hashCode() % 6 + 6) % 6, size = 36, label = "An")
                Column(Modifier.weight(1f)) {
                    Text(
                        "Anonim · device #${report.id.takeLast(4)}",
                        fontWeight = FontWeight.W600, fontSize = 14.sp, color = Ink,
                    )
                    Text(
                        "Sudah ${ReportSeed.size} laporan valid · trust score 87/100",
                        style = AlirinText.caption,
                    )
                }
            }
        }
    }
}

// ── Action bar (fixed bottom) ──────────────────────────────────
@Composable
private fun ActionBar(
    onReject: () -> Unit,
    onSchedule: () -> Unit,
    onVerify: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .background(Surface)
    ) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(Hairline))
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ActionBarButton(
                label = "Tolak",
                bg = Surface2,
                ink = Ink,
                onClick = onReject,
                modifier = Modifier.weight(1f),
            )
            ActionBarButton(
                label = "Jadwalkan",
                bg = Surface2,
                ink = Ink,
                onClick = onSchedule,
                modifier = Modifier.weight(1f),
            )
            ActionBarButton(
                label = "Verifikasi",
                bg = Primary,
                ink = Color.White,
                leadingIcon = AlirinIcons.check,
                onClick = onVerify,
                modifier = Modifier.weight(2f),
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ActionBarButton(
    label: String,
    bg: Color,
    ink: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
) {
    Row(
        modifier
            .height(46.dp)
            .clip(Radius.pill)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
    ) {
        if (leadingIcon != null) Icon(leadingIcon, null, tint = ink, modifier = Modifier.size(18.dp))
        Text(label, color = ink, fontWeight = FontWeight.W600, fontSize = 14.sp)
    }
}

// ── Action sheets ──────────────────────────────────────────────
@Composable
private fun SheetHandle() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(width = 36.dp, height = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Hairline2)
        )
    }
}

@Composable
private fun SheetFooter(
    confirmLabel: String,
    confirmTone: Color,
    onClose: () -> Unit,
    onConfirm: () -> Unit,
) {
    Row(
        Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AlirinButton(label = "Batal", onClick = onClose, variant = BtnVariant.Soft, modifier = Modifier.weight(1f))
        Row(
            Modifier
                .weight(2f)
                .height(50.dp)
                .clip(Radius.pill)
                .background(confirmTone)
                .clickable(onClick = onConfirm)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(confirmLabel, color = Color.White, fontWeight = FontWeight.W600, fontSize = 14.sp)
        }
    }
}

@Composable
private fun VerifySheet(onClose: () -> Unit, onConfirm: (String?) -> Unit) {
    var note by rememberSaveable { mutableStateOf("") }
    Column(Modifier.padding(horizontal = 20.dp)) {
        SheetHandle()
        Text("Verifikasi laporan?", style = AlirinText.h2, modifier = Modifier.padding(bottom = 4.dp))
        Text(
            "Status laporan akan jadi \"Sudah Diverifikasi\" dan pelapor mendapat notifikasi.",
            style = AlirinText.bodyR,
            modifier = Modifier.padding(bottom = 18.dp),
        )
        Text("Catatan internal (opsional)", style = AlirinText.eyebrow, modifier = Modifier.padding(bottom = 8.dp))
        TextField(
            value = note,
            onValueChange = { note = it },
            placeholder = "Catatan untuk tim tindak lanjut...",
            singleLine = false,
            minHeight = 80,
        )
    }
    Spacer(Modifier.height(16.dp))
    SheetFooter(
        confirmLabel = "Verifikasi sekarang",
        confirmTone = Primary,
        onClose = onClose,
        onConfirm = { onConfirm(if (note.isBlank()) null else note) },
    )
}

@Composable
private fun RejectSheet(onClose: () -> Unit, onConfirm: (String) -> Unit) {
    val reasons = listOf(
        "duplikat" to "Duplikat — sudah ada laporan sama",
        "invalid"  to "Tidak valid — bukan masalah drainase",
        "unclear"  to "Foto / lokasi tidak jelas",
        "other"    to "Lainnya",
    )
    var reason by rememberSaveable { mutableStateOf(reasons.first().first) }
    Column(Modifier.padding(horizontal = 20.dp)) {
        SheetHandle()
        Text("Tolak laporan?", style = AlirinText.h2, modifier = Modifier.padding(bottom = 4.dp))
        Text(
            "Pelapor akan diberi tahu dengan alasan penolakan.",
            style = AlirinText.bodyR,
            modifier = Modifier.padding(bottom = 18.dp),
        )
        Text("Alasan", style = AlirinText.eyebrow, modifier = Modifier.padding(bottom = 8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 4.dp)) {
            reasons.forEach { (id, label) ->
                RadioRow(label = label, active = reason == id, onClick = { reason = id })
            }
        }
    }
    Spacer(Modifier.height(16.dp))
    SheetFooter(
        confirmLabel = "Tolak laporan",
        confirmTone = RiskKritisDot,
        onClose = onClose,
        onConfirm = { onConfirm(reasons.find { it.first == reason }?.second ?: reason) },
    )
}

@Composable
private fun ScheduleSheet(onClose: () -> Unit, onConfirm: (team: String, time: String) -> Unit) {
    val teams = listOf("A" to "Tim A · Kebersihan", "B" to "Tim B · Drainase", "C" to "Tim C · PUPR")
    val times = listOf(
        "today-pm" to "Hari ini · sore (14:00–17:00)",
        "tomorrow-am" to "Besok · pagi (08:00–11:00)",
        "this-week" to "Minggu ini · kapan saja",
    )
    var team by rememberSaveable { mutableStateOf(teams.first().first) }
    var time by rememberSaveable { mutableStateOf(times.first().first) }
    Column(Modifier.padding(horizontal = 20.dp)) {
        SheetHandle()
        Text("Jadwalkan tindakan", style = AlirinText.h2, modifier = Modifier.padding(bottom = 4.dp))
        Text(
            "Pilih tim & waktu. Status akan otomatis ke \"Dijadwalkan\".",
            style = AlirinText.bodyR,
            modifier = Modifier.padding(bottom = 18.dp),
        )
        Text("Tim", style = AlirinText.eyebrow, modifier = Modifier.padding(bottom = 8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 14.dp)) {
            teams.forEach { (id, label) ->
                TeamChip(label = label, active = team == id, onClick = { team = id }, modifier = Modifier.weight(1f))
            }
        }
        Text("Waktu", style = AlirinText.eyebrow, modifier = Modifier.padding(bottom = 8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            times.forEach { (id, label) ->
                RadioRow(label = label, active = time == id, onClick = { time = id })
            }
        }
    }
    Spacer(Modifier.height(16.dp))
    SheetFooter(
        confirmLabel = "Jadwalkan & verifikasi",
        confirmTone = Primary,
        onClose = onClose,
        onConfirm = {
            val teamLabel = teams.find { it.first == team }?.second.orEmpty()
            val timeLabel = times.find { it.first == time }?.second.orEmpty()
            onConfirm(teamLabel, timeLabel)
        },
    )
}

@Composable
private fun RadioRow(label: String, active: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(Radius.md)
            .background(if (active) Surface2 else Color.Transparent)
            .border(1.dp, if (active) Ink else Hairline, Radius.md)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier
                .size(22.dp)
                .clip(CircleShape)
                .border(2.dp, if (active) Ink else Hairline2, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (active) Box(Modifier.size(8.dp).clip(CircleShape).background(Ink))
        }
        Text(label, fontSize = 13.5.sp, fontWeight = FontWeight.W500, color = Ink)
    }
}

@Composable
private fun TeamChip(label: String, active: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(Radius.md)
            .background(if (active) Ink else Surface)
            .then(if (!active) Modifier.border(1.dp, Hairline, Radius.md) else Modifier)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = if (active) Color.White else Ink,
            fontSize = 12.sp,
            fontWeight = FontWeight.W600,
            letterSpacing = (-0.06).sp,
        )
    }
}

// ── Success screen ─────────────────────────────────────────────
@Composable
private fun StaffActionSuccess(action: SheetKind, report: Report, onDone: () -> Unit) {
    val (headline, body, label, isReject) = when (action) {
        SheetKind.Verify -> Quad(
            "Laporan tervalidasi 🎉",
            "Status ${report.code} sekarang \"Sudah Diverifikasi\". Pelapor sudah dapat notifikasi.",
            "Diverifikasi",
            false,
        )
        SheetKind.Schedule -> Quad(
            "Tim sudah dijadwalkan",
            "Tim akan turun ke lokasi sesuai jadwal. Pelapor akan dapat update otomatis.",
            "Dijadwalkan",
            false,
        )
        SheetKind.Reject -> Quad(
            "Laporan ditolak",
            "Pelapor diberi tahu alasan penolakan. Laporan tidak akan diteruskan.",
            "Ditolak",
            true,
        )
    }
    Column(Modifier.fillMaxSize().background(Bg)) {
        Row(
            Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Spacer(Modifier.size(40.dp))
            Text("Aksi terkirim", fontWeight = FontWeight.W600, fontSize = 15.sp, color = Ink)
            AlirinIconBubble(icon = AlirinIcons.close, onClick = onDone, bg = Surface2)
        }
        Column(
            Modifier
                .weight(1f)
                .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.padding(bottom = 18.dp),
            ) {
                Box(
                    Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isReject) RiskKritisDot else Primary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        if (isReject) AlirinIcons.close else AlirinIcons.check,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp),
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(headline, fontSize = 22.sp, fontWeight = FontWeight.W700, color = Ink, letterSpacing = (-0.44).sp, lineHeight = 26.sp)
                    Text("${report.code} · ${report.category}", style = AlirinText.caption, modifier = Modifier.padding(top = 2.dp))
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(Hairline))
            Row(
                Modifier.fillMaxWidth().padding(vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SuccessKv(label = "Status baru", value = label, modifier = Modifier.weight(1f))
                SuccessKv(label = "Oleh", value = "Staff", modifier = Modifier.weight(1f))
                SuccessKv(label = "Waktu", value = "Baru saja", align = Alignment.End, modifier = Modifier.weight(1f))
            }
            Text(body, color = Ink2, fontSize = 14.sp, lineHeight = 22.sp, modifier = Modifier.padding(bottom = 18.dp))
            if (action == SheetKind.Verify) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(Radius.md)
                        .background(PrimarySofter)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(AlirinIcons.users, null, tint = Primary, modifier = Modifier.size(22.dp))
                    Text(
                        "Pelapor di lokasi ini otomatis dapat update status.",
                        color = PrimaryInk,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.W500,
                        lineHeight = 18.sp,
                    )
                }
            }
        }
        Column(Modifier.padding(start = 20.dp, end = 20.dp, bottom = 20.dp)) {
            AlirinButton(label = "Selesai", onClick = onDone, block = true)
        }
    }
}

@Composable
private fun SuccessKv(label: String, value: String, align: Alignment.Horizontal = Alignment.Start, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = align) {
        Text(label, style = AlirinText.caption, modifier = Modifier.padding(bottom = 4.dp))
        Text(value, fontWeight = FontWeight.W700, fontSize = 14.sp, color = Ink, letterSpacing = (-0.14).sp)
    }
}

/** Tiny tuple used in StaffActionSuccess destructuring. */
private data class Quad(val a: String, val b: String, val c: String, val d: Boolean)
