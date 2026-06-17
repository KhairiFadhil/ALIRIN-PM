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
import com.example.alirinmobile.data.network.dto.AiForecast
import com.example.alirinmobile.data.repository.Kelurahan
import com.example.alirinmobile.data.repository.NearbyTeaserSeed
import com.example.alirinmobile.data.repository.PredictionUiModel
import com.example.alirinmobile.data.repository.WeatherState
import com.example.alirinmobile.feature.AlirinViewModelFactory
import com.example.alirinmobile.feature.PredictionViewModel
import com.example.alirinmobile.feature.WeatherViewModel
import com.example.alirinmobile.ui.components.*
import com.example.alirinmobile.ui.theme.*
import kotlin.math.ceil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BerandaScreen(
    reports: List<Report>,
    session: com.example.alirinmobile.data.auth.AuthSession?,
    onLaporClick: () -> Unit,
    onPetaClick: () -> Unit,
    onStatusClick: () -> Unit,
    onStatusItemClick: (Report) -> Unit,
    onTentangClick: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {

    val weatherVm: WeatherViewModel = viewModel(factory = AlirinViewModelFactory.Factory)
    val selected by weatherVm.selected.collectAsStateWithLifecycle()
    val areaKecamatan = selected?.kecamatan ?: "Bandar Lampung"

    val kritisCount = reports.count { it.risk == RiskLevel.Kritis }
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val activeCount = reports.count { it.status != ReportStatus.Completed && it.status != ReportStatus.Rejected }

    val loggedIn = session != null
    val displayName = session?.displayName ?: "Warga"
    val initials = session?.displayName?.trim()?.split(" ")
        ?.mapNotNull { it.firstOrNull() }?.take(2)?.joinToString("")?.uppercase()

    var showAccount by remember { mutableStateOf(false) }

    Column(
        modifier
            .fillMaxSize()
            .background(Bg)
            .verticalScroll(rememberScrollState())
    ) {
        Greeting(
            loggedIn = loggedIn,
            initials = initials,
            title = if (loggedIn) "Halo, $displayName" else "Warga",
            subtitle = if (loggedIn) "Akun aktif" else "Mode anonim - ketuk untuk akun",
            hasNotif = activeCount > 0,
            onProfile = { showAccount = true },
            onBell = {
                ctx.toast(
                    if (activeCount > 0) "$activeCount laporan kamu sedang diproses." else "Belum ada notifikasi baru.",
                )
            },
        )

        if (showAccount) {
            AccountSheet(
                loggedIn = loggedIn,
                displayName = displayName,
                roleLabel = session?.role?.name ?: "Anonim",
                onDismiss = { showAccount = false },
                onTentang = { showAccount = false; onTentangClick() },
                onLogout = { showAccount = false; onLogout() },
            )
        }
        Column(
            Modifier
                .padding(start = Space.s5, end = Space.s5, bottom = Space.s6),
            verticalArrangement = Arrangement.spacedBy(Space.s4),
        ) {

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
            NearbyStrip(items = NearbyTeaserSeed, onOpenMap = onPetaClick)
            MyReports(recent = reports, onSeeAll = onStatusClick, onItemClick = onStatusItemClick)
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun Greeting(
    loggedIn: Boolean,
    initials: String?,
    title: String,
    subtitle: String,
    hasNotif: Boolean,
    onProfile: () -> Unit,
    onBell: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = Space.s5, end = Space.s5, top = Space.s1, bottom = Space.s5),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Space.s3),
    ) {

        Box(
            Modifier
                .size(42.dp)
                .clip(CircleShape)
                .clickable(onClick = onProfile),
            contentAlignment = Alignment.Center,
        ) {
            if (loggedIn && initials != null) {
                Avatar(seed = 3, size = 42, label = initials)
            } else {
                Box(
                    Modifier.matchParentSize().clip(CircleShape).background(Surface2),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(AlirinIcons.users, null, tint = Muted, modifier = Modifier.size(22.dp))
                }
            }
        }
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.W700, color = Ink, letterSpacing = (-0.24).sp, maxLines = 1)
            Text(subtitle, color = Muted, fontWeight = FontWeight.W500, fontSize = 12.5.sp, maxLines = 1)
        }

        Box(Modifier.size(40.dp)) {
            Box(
                Modifier
                    .matchParentSize()
                    .clip(CircleShape)
                    .background(Surface2)
                    .clickable(onClick = onBell),
                contentAlignment = Alignment.Center,
            ) {
                Icon(AlirinIcons.bell, null, tint = Ink, modifier = Modifier.size(20.dp))
            }
            if (hasNotif) {
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountSheet(
    loggedIn: Boolean,
    displayName: String,
    roleLabel: String,
    onDismiss: () -> Unit,
    onTentang: () -> Unit,
    onLogout: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = Radius.sheet,
        containerColor = Surface,
    ) {
        Column(
            Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                    Modifier.size(48.dp).clip(CircleShape).background(Surface2),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(AlirinIcons.users, null, tint = if (loggedIn) Primary else Muted, modifier = Modifier.size(26.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        if (loggedIn) displayName else "Warga Anonim",
                        fontSize = 16.sp, fontWeight = FontWeight.W700, color = Ink,
                    )
                    Text(
                        if (loggedIn) "Masuk sebagai $roleLabel" else "Belum masuk - lapor tanpa akun",
                        style = AlirinText.caption,
                    )
                }
            }

            if (!loggedIn) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(Radius.md)
                        .background(PrimarySofter)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(AlirinIcons.info, null, tint = PrimaryInk, modifier = Modifier.size(18.dp))
                    Text(
                        "Kamu melapor sebagai warga anonim. Petugas bisa masuk lewat tombol di bawah.",
                        color = PrimaryInk, fontSize = 12.5.sp, lineHeight = 18.sp,
                    )
                }
            }

            AlirinButton(
                label = "Tentang ALIRIN",
                onClick = onTentang,
                variant = BtnVariant.Soft,
                block = true,
                leadingIcon = AlirinIcons.info,
            )
            AlirinButton(
                label = if (loggedIn) "Keluar" else "Masuk sebagai Staff / Admin",
                onClick = onLogout,
                block = true,
                leadingIcon = if (loggedIn) AlirinIcons.arrowLeft else AlirinIcons.shield,
            )
        }
    }
}

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
                    Text("Lapormu", color = SkySoft, fontSize = 11.5.sp, fontWeight = FontWeight.W500)
                    Text("3 laporan", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.W700)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total area", color = SkySoft, fontSize = 11.5.sp, fontWeight = FontWeight.W500)
                    Text("13 laporan", color = PrimaryOnDark, fontSize = 18.sp, fontWeight = FontWeight.W700)
                }
            }
            SegmentedProgress(filled = 3, total = 10, modifier = Modifier.padding(bottom = 16.dp))

            Row(
                Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .clip(Radius.pill)
                    .background(Primary2),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            ) {
                Icon(AlirinIcons.plus, null, tint = Color.White, modifier = Modifier.size(18.dp))
                Text("Buat laporan baru", color = Color.White, fontWeight = FontWeight.W600, fontSize = 14.5.sp)
            }
        }
    }
}

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

@Composable
private fun PredictionCard(onOpenMap: () -> Unit) {
    val vm: PredictionViewModel = viewModel(factory = AlirinViewModelFactory.Factory)
    val prediction by vm.prediction.collectAsStateWithLifecycle()
    val p = prediction
    if (p == null) PredictionCardSkeleton()
    else PredictionCardContent(model = p, onOpenMap = onOpenMap)
}

@Composable
private fun PredictionCardSkeleton() {
    AlirinCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(18.dp)) {
        Column {
            Text("PREDIKSI AI 3 JAM KE DEPAN", style = AlirinText.eyebrow)
            Text("Memuat prakiraan dari BMKG + GROQ...", style = AlirinText.bodyR, modifier = Modifier.padding(top = 6.dp))
        }
    }
}

@Composable
private fun PredictionCardContent(model: PredictionUiModel, onOpenMap: () -> Unit) {
    val ai = model.ai
    val riskTint = when {
        ai.curahHujanMm >= 10.0 -> RiskKritisDot to RiskKritisBg
        ai.curahHujanMm >= 5.0  -> RiskTinggiDot to RiskTinggiBg
        ai.curahHujanMm >= 1.0  -> RiskWaspadaDot to RiskWaspadaBg
        else                    -> RiskNormalDot to RiskNormalBg
    }
    val sourceLabel = when (model.source) {
        PredictionUiModel.Source.Groq     -> "GROQ AI"
        PredictionUiModel.Source.Fallback -> "Rule-based"
        PredictionUiModel.Source.Loading  -> "Memuat AI..."
    }
    Box(
        Modifier
            .fillMaxWidth()
            .clip(Radius.lg)
            .background(riskTint.second)
            .clickable(onClick = onOpenMap)
            .padding(18.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "PREDIKSI AI 3 JAM KE DEPAN",
                    style = AlirinText.eyebrow.copy(color = Ink),
                    modifier = Modifier.weight(1f),
                )
                Pill(
                    label = sourceLabel,
                    bg = Color.White.copy(alpha = 0.7f),
                    ink = Ink2,
                    leadingIcon = AlirinIcons.sparkles,
                )
            }

            Text(
                ai.ringkasan,
                color = Ink,
                fontWeight = FontWeight.W700,
                fontSize = 16.sp,
                lineHeight = 22.sp,
                letterSpacing = (-0.16).sp,
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                AiStatRow("Kondisi udara",  ai.kondisiUdara,                       accent = riskTint.first)
                AiStatRow("Suhu",            "${"%.1f".format(ai.suhuCelsius)} °C", accent = riskTint.first)
                AiStatRow("Curah hujan 3h",  "${"%.1f".format(ai.curahHujanMm)} mm", accent = riskTint.first)
                AiStatRow("Debit air est.",  "${"%.3f".format(ai.debitAirMs)} m³/s", accent = riskTint.first)
            }

            if (ai.rekomendasi.isNotEmpty()) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(Radius.md)
                        .background(Color.White.copy(alpha = 0.6f))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(AlirinIcons.sparkles, null, tint = riskTint.first, modifier = Modifier.size(14.dp))
                        Text("REKOMENDASI AI", style = AlirinText.eyebrow.copy(color = Ink2))
                    }
                    ai.rekomendasi.take(4).forEach { rec ->
                        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.padding(top = 6.dp).size(5.dp).clip(CircleShape).background(riskTint.first))
                            Text(rec, color = Ink, fontSize = 12.5.sp, lineHeight = 17.sp, fontWeight = FontWeight.W500)
                        }
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Sumber: BMKG + GROQ LLM · update tiap fetch",
                    style = AlirinText.caption,
                    modifier = Modifier.weight(1f),
                )
                Icon(AlirinIcons.chevronRight, null, tint = Ink, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun AiStatRow(label: String, value: String, accent: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(accent))
        Text(
            label,
            color = Ink2,
            fontSize = 12.sp,
            fontWeight = FontWeight.W500,
            modifier = Modifier.padding(start = 8.dp).weight(1f),
        )
        Text(
            value,
            color = Ink,
            fontWeight = FontWeight.W700,
            fontSize = 12.5.sp,
        )
    }
}

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
    val subColor = if (primaryFeatured) SkySoft else Muted

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
                            color = Surface,
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
                modifier = Modifier.fillMaxWidth().border(1.dp, Hairline2, Radius.lg),
                padding = PaddingValues(24.dp),
            ) {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(AlirinIcons.document, null, tint = Faint, modifier = Modifier.size(36.dp).padding(bottom = 8.dp))
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
