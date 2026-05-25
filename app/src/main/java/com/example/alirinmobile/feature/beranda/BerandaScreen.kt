package com.example.alirinmobile.feature.beranda

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.alirinmobile.data.*
import com.example.alirinmobile.data.ml.Factor
import com.example.alirinmobile.data.ml.PredictionResult
import com.example.alirinmobile.feature.AlirinViewModelFactory
import com.example.alirinmobile.feature.PredictionViewModel
import com.example.alirinmobile.feature.WeatherViewModel
import com.example.alirinmobile.ui.components.*
import com.example.alirinmobile.ui.theme.*
import kotlin.math.ceil

@Composable
fun BerandaScreen(
    reports: List<Report>,
    areaKecamatan: String = "Kemiling",
    onLaporClick: () -> Unit,
    onPetaClick: () -> Unit,
    onStatusClick: () -> Unit,
    onStatusItemClick: (Report) -> Unit,
    onTentangClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val kritisCount = reports.count { it.risk == RiskLevel.Kritis }
    Column(
        modifier
            .fillMaxSize()
            .background(Bg)
            .verticalScroll(rememberScrollState())
    ) {
        Greeting(area = areaKecamatan)
        Column(
            Modifier
                .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Grid hero: eyebrow + display title (no big green CTA card)
            Column {
                Text(
                    "Apa kabar drainase hari ini?",
                    style = AlirinText.eyebrow,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
                val highlight = if (kritisCount > 0) "$kritisCount titik kritis" else "Drainase aman"
                Text(
                    "$highlight\ndi $areaKecamatan.",
                    style = AlirinText.display,
                )
            }

            // 2x2 grid: Lapor Cepat / Lapor Lengkap / Peta / Status
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ActionCell(
                        icon = AlirinIcons.bolt,
                        label = "Lapor Cepat",
                        sub = "±10 detik, tanpa foto",
                        accent = ActionAccent.Indigo,
                        modifier = Modifier.weight(1f),
                        onClick = onLaporClick,
                    )
                    ActionCell(
                        icon = AlirinIcons.camera,
                        label = "Lapor Lengkap",
                        sub = "60s, dengan bukti foto",
                        accent = ActionAccent.Amber,
                        modifier = Modifier.weight(1f),
                        onClick = onLaporClick,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ActionCell(
                        icon = AlirinIcons.map,
                        label = "Peta Risiko",
                        sub = "Titik di sekitar",
                        modifier = Modifier.weight(1f),
                        onClick = onPetaClick,
                    )
                    ActionCell(
                        icon = AlirinIcons.document,
                        label = "Status",
                        sub = "Riwayat laporan",
                        modifier = Modifier.weight(1f),
                        onClick = onStatusClick,
                    )
                }
            }

            WeatherStrip()
            PredictionCard(onOpenMap = onPetaClick)
            NearbyStrip(items = SampleData.nearbyTeasers, onOpenMap = onPetaClick)
            MyReports(recent = reports, onSeeAll = onStatusClick, onItemClick = onStatusItemClick)
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── Greeting ────────────────────────────────────────────────────
@Composable
private fun Greeting(area: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Avatar(seed = 0, size = 42, label = "BL", badge = true)
        Column(Modifier.weight(1f)) {
            Text("Beranda", style = AlirinText.caption)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(area, fontSize = 18.sp, fontWeight = FontWeight.W700, color = Ink, letterSpacing = (-0.27).sp)
                Text(
                    text = " · Bandar Lampung",
                    color = Muted,
                    fontWeight = FontWeight.W500,
                    fontSize = 13.5.sp,
                )
            }
        }
        // Bell button with notification dot.
        // The dot lives in an outer wrapper (not inside the clipped circle) so it
        // sits flush against the top-right edge without being chopped.
        Box(Modifier.size(40.dp)) {
            Box(
                Modifier
                    .matchParentSize()
                    .clip(CircleShape)
                    .background(Surface2)
                    .clickable { /* TODO: notifications screen */ },
                contentAlignment = Alignment.Center,
            ) {
                Icon(AlirinIcons.bell, null, tint = Ink, modifier = Modifier.size(20.dp))
            }
            Box(
                Modifier
                    .size(10.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = (-4).dp, y = 4.dp)
                    .clip(CircleShape)
                    .background(RiskKritisDot)
                    .border(2.dp, Surface, CircleShape)
            )
        }
    }
}

// ── Hero CTA (bike-rods style) ─────────────────────────────────
@Composable
private fun PrimaryReportCTA(onClick: () -> Unit) {
    AlirinGreenCard(
        modifier = Modifier.fillMaxWidth(),
        padding = PaddingValues(start = 18.dp, end = 18.dp, top = 18.dp, bottom = 14.dp),
        onClick = onClick,
    ) {
        Column {
            Row(
                Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                PillOnDark(label = "Minggu ini · 7 hari aktif")
                AvatarStack(count = 3, size = 26, dark = true)
            }
            Text(
                "Laporkan drainase",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.W700,
                letterSpacing = (-0.65).sp,
                modifier = Modifier.padding(bottom = 14.dp),
            )
            Row(
                Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Column {
                    Text("Lapormu", color = Color.White.copy(alpha = 0.65f), fontSize = 11.5.sp, fontWeight = FontWeight.W500)
                    Text("3 laporan", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.W700)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total area", color = Color.White.copy(alpha = 0.65f), fontSize = 11.5.sp, fontWeight = FontWeight.W500)
                    Text("13 laporan", color = PrimaryOnDark, fontSize = 18.sp, fontWeight = FontWeight.W700)
                }
            }
            SegmentedProgress(filled = 3, total = 10, modifier = Modifier.padding(bottom = 16.dp))
            // Inner pill button
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(Radius.pill)
                    .background(Color.Black.copy(alpha = 0.25f)),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            ) {
                Icon(AlirinIcons.plus, null, tint = Color.White, modifier = Modifier.size(18.dp))
                Text("Buat laporan baru", color = Color.White, fontWeight = FontWeight.W600, fontSize = 14.5.sp)
            }
        }
    }
}

// ── Action cell (flat icon, no rounded-square background) ───────
enum class ActionAccent { Default, Indigo, Amber }

@Composable
private fun ActionCell(
    icon: ImageVector,
    label: String,
    sub: String,
    accent: ActionAccent = ActionAccent.Default,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val iconTint = when (accent) {
        ActionAccent.Indigo -> Primary
        ActionAccent.Amber  -> RiskTinggiDot
        ActionAccent.Default -> Ink2
    }
    AlirinFlatCard(
        modifier = modifier.defaultMinSize(minHeight = 108.dp).clip(Radius.lg),
        padding = PaddingValues(14.dp),
        onClick = onClick,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(22.dp))
            Column {
                Text(label, fontWeight = FontWeight.W700, fontSize = 14.sp, color = Ink, letterSpacing = (-0.14).sp)
                Text(sub, style = AlirinText.caption, modifier = Modifier.padding(top = 2.dp))
            }
        }
    }
}

// ── Weather strip — backed by real BMKG forecast ────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeatherStrip() {
    val vm: WeatherViewModel = viewModel(factory = AlirinViewModelFactory.Factory)
    val state by vm.state.collectAsStateWithLifecycle()
    val selected by vm.selected.collectAsStateWithLifecycle()
    var showPicker by remember { mutableStateOf(false) }

    val (title, subtitle) = weatherStripLabels(state, selected)

    Row(
        Modifier
            .fillMaxWidth()
            .clip(Radius.lg)
            .background(SkySoft)
            .border(1.dp, SkyBorder, Radius.lg)
            .clickable { showPicker = true }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(AlirinIcons.cloud, null, tint = SkyInk, modifier = Modifier.size(26.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = SkyInk, fontWeight = FontWeight.W700, fontSize = 14.sp, maxLines = 1)
            Text(subtitle, color = SkyInk.copy(alpha = 0.7f), fontWeight = FontWeight.W500, fontSize = 11.5.sp, modifier = Modifier.padding(top = 1.dp), maxLines = 1)
        }
        Icon(AlirinIcons.chevronRight, null, tint = SkyInk, modifier = Modifier.size(18.dp))
    }

    if (showPicker) {
        ModalBottomSheet(
            onDismissRequest = { showPicker = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape = Radius.sheet,
            containerColor = Surface,
            dragHandle = null,
        ) {
            KelurahanPickerSheet(
                items = vm.list,
                selected = selected,
                onPick = { vm.pick(it); showPicker = false },
            )
        }
    }
}

private fun weatherStripLabels(
    state: WeatherState,
    selected: Kelurahan?,
): Pair<String, String> {
    val areaLabel = selected?.let { "${it.kelurahan}, ${it.kecamatan}" } ?: "Bandar Lampung"
    return when (state) {
        WeatherState.Idle, WeatherState.Loading ->
            "Memuat prakiraan BMKG..." to areaLabel
        is WeatherState.Error ->
            "BMKG tidak tersedia" to areaLabel
        is WeatherState.Loaded -> {
            val hour = state.data.data.firstOrNull()
                ?.cuaca?.firstOrNull()
                ?.firstOrNull()
            val desc = hour?.weatherDesc?.takeIf { it.isNotBlank() } ?: "Prakiraan terbaru"
            val temp = hour?.t?.let { "$it°C" }.orEmpty()
            val precip = hour?.tp?.takeIf { it > 0.0 }?.let { "· hujan ${"%.1f".format(it)} mm" }.orEmpty()
            val title = buildString {
                append(desc)
                if (temp.isNotBlank()) append(" · ").append(temp)
            }
            val sub = buildString {
                append("BMKG · ").append(areaLabel)
                if (precip.isNotBlank()) append(" ").append(precip)
            }
            title to sub
        }
    }
}

@Composable
private fun KelurahanPickerSheet(
    items: List<Kelurahan>,
    selected: Kelurahan?,
    onPick: (Kelurahan) -> Unit,
) {
    Column(Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 24.dp)) {
        Box(
            Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 12.dp)
                .size(width = 36.dp, height = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Hairline2)
        )
        Text("Pilih kelurahan", style = AlirinText.h3, modifier = Modifier.padding(bottom = 6.dp))
        Text(
            "Forecast BMKG di-fetch ulang per kelurahan.",
            style = AlirinText.caption,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        LazyColumn(
            modifier = Modifier.heightIn(max = 420.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(items) { k ->
                val active = selected?.adm4 == k.adm4
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(Radius.md)
                        .background(if (active) PrimarySoft else Color.Transparent)
                        .clickable { onPick(k) }
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(
                        Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (active) Primary else Surface2),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            AlirinIcons.pin,
                            null,
                            tint = if (active) Color.White else Ink2,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        Text(k.kelurahan, fontWeight = FontWeight.W700, fontSize = 14.sp, color = Ink)
                        Text(
                            "${k.kecamatan} · ${k.adm4}",
                            style = AlirinText.caption.copy(fontSize = 11.sp),
                        )
                    }
                    if (active) Icon(AlirinIcons.check, null, tint = Primary, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// ── Prediction card (rule-based ML, future TFLite) ──────────────
@Composable
private fun PredictionCard(onOpenMap: () -> Unit) {
    val vm: PredictionViewModel = viewModel(factory = AlirinViewModelFactory.Factory)
    val prediction by vm.prediction.collectAsStateWithLifecycle()
    val p = prediction ?: return PredictionCardSkeleton()
    PredictionCardContent(prediction = p, onOpenMap = onOpenMap)
}

@Composable
private fun PredictionCardSkeleton() {
    AlirinCard(
        modifier = Modifier.fillMaxWidth(),
        padding = PaddingValues(18.dp),
    ) {
        Column {
            Text("PREDIKSI 3 JAM KE DEPAN", style = AlirinText.eyebrow)
            Text("Menghitung dari BMKG + laporan...", style = AlirinText.bodyR, modifier = Modifier.padding(top = 6.dp))
        }
    }
}

@Composable
private fun PredictionCardContent(prediction: PredictionResult, onOpenMap: () -> Unit) {
    val accent = prediction.level.dot
    val softBg = prediction.level.bg
    Box(
        Modifier
            .fillMaxWidth()
            .clip(Radius.lg)
            .background(softBg)
            .clickable(onClick = onOpenMap)
            .padding(18.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "PREDIKSI 3 JAM KE DEPAN",
                    style = AlirinText.eyebrow.copy(color = prediction.level.ink),
                    modifier = Modifier.weight(1f),
                )
                Icon(AlirinIcons.sparkles, null, tint = prediction.level.ink, modifier = Modifier.size(16.dp))
            }
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = prediction.score.toString(),
                    color = prediction.level.ink,
                    fontWeight = FontWeight.W800,
                    fontSize = 44.sp,
                    letterSpacing = (-1.2).sp,
                )
                Text(
                    "/ 100",
                    color = prediction.level.ink.copy(alpha = 0.55f),
                    fontWeight = FontWeight.W600,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                Spacer(Modifier.weight(1f))
                RiskPill(level = prediction.level, score = null)
            }
            Text(
                "Risiko ${prediction.level.label.lowercase()} di ${prediction.areaLabel}.",
                color = prediction.level.ink,
                fontWeight = FontWeight.W600,
                fontSize = 13.5.sp,
            )
            // Factor mini-bars
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                prediction.factors.forEach { f ->
                    FactorRow(factor = f, accent = accent, inkColor = prediction.level.ink)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Berbasis BMKG + ${SampleData.hotspots.size} titik historis",
                    style = AlirinText.caption.copy(color = prediction.level.ink.copy(alpha = 0.7f)),
                    modifier = Modifier.weight(1f),
                )
                Icon(AlirinIcons.chevronRight, null, tint = prediction.level.ink, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun FactorRow(factor: Factor, accent: Color, inkColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            factor.label,
            color = inkColor.copy(alpha = 0.8f),
            fontSize = 11.5.sp,
            fontWeight = FontWeight.W500,
            modifier = Modifier.weight(1f),
            maxLines = 1,
        )
        Text(
            factor.rawValue,
            color = inkColor.copy(alpha = 0.7f),
            fontSize = 11.5.sp,
            fontWeight = FontWeight.W500,
        )
        // Mini bar (segments out of MAX_WEIGHT)
        Row(
            Modifier.width(56.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            val maxWeight = 50
            val filled = ((factor.weight.toFloat() / maxWeight) * 6f).toInt().coerceIn(0, 6)
            repeat(6) { i ->
                Box(
                    Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (i < filled) accent else inkColor.copy(alpha = 0.18f))
                )
            }
        }
    }
}

// ── Nearby strip (horizontal list) ──────────────────────────────
@Composable
private fun NearbyStrip(items: List<NearbyTeaser>, onOpenMap: () -> Unit) {
    Column {
        SectionHeader(
            eyebrow = "Sekitar kamu · 1 km",
            title = "Titik rawan terdekat",
            action = "Buka peta",
            onAction = onOpenMap,
        )
        LazyRow(
            contentPadding = PaddingValues(start = 0.dp, end = 0.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(items.withIndex().toList()) { (idx, it) ->
                NearbyCard(item = it, primaryFeatured = idx == 0, onClick = onOpenMap)
            }
        }
    }
}

@Composable
private fun NearbyCard(item: NearbyTeaser, primaryFeatured: Boolean, onClick: () -> Unit) {
    val cardBg = if (primaryFeatured) Primary else Surface
    val titleColor = if (primaryFeatured) Color.White else Ink
    val subColor = if (primaryFeatured) Color.White.copy(alpha = 0.7f) else Muted

    Box(
        Modifier
            .width(240.dp)
            .clip(Radius.lg)
            .background(cardBg)
            .then(if (!primaryFeatured) Modifier.border(1.dp, Hairline, Radius.lg) else Modifier)
            .clickable(onClick = onClick)
            .padding(14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                if (primaryFeatured) {
                    Row(
                        Modifier
                            .height(24.dp)
                            .clip(Radius.pill)
                            .background(Color.White.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Dot(color = Color.White)
                        Text(
                            "${item.risk.label} · ${item.score}",
                            color = Color.White.copy(alpha = 0.95f),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.W600,
                        )
                    }
                } else {
                    RiskPill(level = item.risk, score = item.score)
                }
                AvatarStack(count = item.count.coerceAtMost(3), size = 22, dark = primaryFeatured)
            }
            Column {
                Text(item.label, color = titleColor, fontWeight = FontWeight.W700, fontSize = 15.sp, letterSpacing = (-0.22).sp)
                Text(
                    "${item.kind} · ${item.count} laporan · ${item.dist}",
                    color = subColor,
                    fontWeight = FontWeight.W500,
                    fontSize = 11.5.sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            val onCount = ceil(item.score / 12.5).toInt().coerceAtMost(8)
            if (primaryFeatured) {
                SegmentedProgress(filled = onCount, total = 8)
            } else {
                SegmentedProgress(
                    filled = onCount,
                    total = 8,
                    onColor = item.risk.dot,
                    offColor = Surface3,
                )
            }
        }
    }
}

// ── My reports list ─────────────────────────────────────────────
@Composable
private fun MyReports(
    recent: List<Report>,
    onSeeAll: () -> Unit,
    onItemClick: (Report) -> Unit,
) {
    Column {
        SectionHeader(
            eyebrow = "Riwayat",
            title = "Laporan kamu",
            action = "Lihat semua",
            onAction = onSeeAll,
        )
        if (recent.isEmpty()) {
            AlirinFlatCard(
                modifier = Modifier.fillMaxWidth(),
                padding = PaddingValues(20.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Kamu belum membuat laporan.", style = AlirinText.body)
                    Text("Mulai laporkan drainase di sekitarmu.", style = AlirinText.caption, modifier = Modifier.padding(top = 6.dp))
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                recent.take(2).forEach { r ->
                    AlirinFlatCard(
                        modifier = Modifier.fillMaxWidth(),
                        padding = PaddingValues(14.dp),
                        onClick = { onItemClick(r) },
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            LogoBadge(size = 42, radius = 11)
                            Column(Modifier.weight(1f)) {
                                Text(r.code, style = AlirinText.monoCode.copy(color = Muted))
                                Text(r.category, fontWeight = FontWeight.W700, fontSize = 14.sp, color = Ink, letterSpacing = (-0.14).sp)
                                Text(r.kelurahan, style = AlirinText.caption, modifier = Modifier.padding(top = 1.dp))
                            }
                            StatusPill(status = r.status)
                        }
                    }
                }
            }
        }
    }
}
