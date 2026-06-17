package com.example.alirinmobile.feature.peta

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.alirinmobile.data.Hotspot
import com.example.alirinmobile.data.HotspotSource
import com.example.alirinmobile.data.Report
import com.example.alirinmobile.data.RiskLevel
import com.example.alirinmobile.data.repository.HotspotSeed
import com.example.alirinmobile.ui.components.*
import com.example.alirinmobile.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetaScreen(
    onBack: () -> Unit,
    onLaporInPlace: (Hotspot?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var filter by remember { mutableStateOf<HotspotSource?>(null) }
    var selected by remember { mutableStateOf<Hotspot?>(null) }
    var query by remember { mutableStateOf("") }

    // Laporan warga (dari database lokal) yang punya koordinat ikut tampil sebagai titik di peta.
    val reportsVm: com.example.alirinmobile.feature.ReportsViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel(factory = com.example.alirinmobile.feature.AlirinViewModelFactory.Factory)
    val reports by reportsVm.reports.collectAsStateWithLifecycle()
    val allHotspots = remember(reports) {
        HotspotSeed + reports.mapIndexedNotNull { i, r -> r.toMapHotspot(10_000 + i) }
    }

    val filtered = allHotspots
        .filter { filter == null || it.src == filter }
        .filter { h ->
            query.isBlank() ||
                h.name.contains(query, ignoreCase = true) ||
                h.kel.contains(query, ignoreCase = true) ||
                h.kec.contains(query, ignoreCase = true)
        }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current

    val weatherVm: com.example.alirinmobile.feature.WeatherViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel(factory = com.example.alirinmobile.feature.AlirinViewModelFactory.Factory)
    val selectedKel by weatherVm.selected.collectAsStateWithLifecycle()
    val areaSummary = remember(filtered, selectedKel) {
        val active = filtered.count { it.count > 0 }.coerceAtLeast(filtered.size.coerceAtMost(1))
        val topScore = filtered.maxOfOrNull { it.score } ?: 0
        val level = when {
            topScore >= 80 -> RiskLevel.Kritis
            topScore >= 60 -> RiskLevel.Tinggi
            topScore >= 40 -> RiskLevel.Waspada
            else -> RiskLevel.Normal
        }
        AreaSummary(
            kecamatan = selectedKel?.kecamatan ?: "Bandar Lampung",
            activeCount = active,
            level = level,
            score = topScore,
        )
    }

    var mapRef by remember { mutableStateOf<org.osmdroid.views.MapView?>(null) }

    val locVm: com.example.alirinmobile.feature.LocationViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel(factory = com.example.alirinmobile.feature.AlirinViewModelFactory.Factory)
    val userLoc by locVm.last.collectAsStateWithLifecycle()
    val userGeo = userLoc?.let { org.osmdroid.util.GeoPoint(it.lat, it.lng) }

    fun goToMyLocation() {
        locVm.fetchOnce { loc ->
            loc?.let {
                mapRef?.controller?.setZoom(15.0)
                mapRef?.controller?.animateTo(org.osmdroid.util.GeoPoint(it.lat, it.lng))
            }
        }
    }

    val locationPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) goToMyLocation() }

    LaunchedEffect(Unit) { if (locVm.hasPermission()) locVm.fetchOnce {} }

    Box(modifier.fillMaxSize()) {

        OsmMapView(
            hotspots = filtered,
            selectedId = selected?.id,
            onHotspotTap = { selected = it },
            modifier = Modifier.fillMaxSize(),
            userLocation = userGeo,
            onMapReady = { mapRef = it },
        )

        Column(
            Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AlirinIconBubble(
                    icon = AlirinIcons.arrowLeft,
                    onClick = onBack,
                    bg = Color.White.copy(alpha = 0.95f),
                )
                Row(
                    Modifier
                        .weight(1f)
                        .height(40.dp)
                        .clip(Radius.md)
                        .background(Color.White.copy(alpha = 0.95f))
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(AlirinIcons.search, null, tint = Muted, modifier = Modifier.size(16.dp))
                    androidx.compose.foundation.text.BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        textStyle = AlirinText.body.copy(color = Ink, fontSize = 14.sp),
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(Primary),
                        modifier = Modifier.weight(1f),
                        decorationBox = { inner ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (query.isEmpty()) Text(
                                    "Cari kecamatan atau kelurahan...",
                                    color = Faint, fontSize = 14.sp,
                                )
                                inner()
                            }
                        },
                    )
                    if (query.isNotEmpty()) {
                        Icon(
                            AlirinIcons.close, null, tint = Muted,
                            modifier = Modifier.size(16.dp).clickable { query = "" },
                        )
                    }
                }
            }

            AreaKamuCard(summary = areaSummary)

            FilterChips(value = filter, onChange = { filter = it })
        }

        Column(
            Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MapButton(icon = AlirinIcons.plus, onClick = { mapRef?.controller?.zoomIn() })
            MapButton(icon = AlirinIcons.layers, onClick = { mapRef?.controller?.zoomOut() })
            MapButton(icon = AlirinIcons.pin, primary = true, onClick = {
                if (locVm.hasPermission()) goToMyLocation()
                else locationPermLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            })
        }

        Column(
            Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 24.dp)
                .clip(Radius.md)
                .background(Color.White.copy(alpha = 0.95f))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text("Level Risiko", style = AlirinText.caption.copy(color = Ink2, fontWeight = FontWeight.W700))
            RiskLevel.entries.reversed().forEach { lvl ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Dot(color = lvl.dot)
                    Text(lvl.label, color = Ink3, fontSize = 11.sp, fontWeight = FontWeight.W500)
                }
            }
        }

        if (selected != null) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { selected = null },
                sheetState = sheetState,
                shape = Radius.sheet,
                containerColor = Surface,
                dragHandle = null,
            ) {
                HotspotSheetContent(
                    hotspot = selected!!,
                    onClose = { selected = null },
                    onAddHere = { onLaporInPlace(selected) },
                )
            }
        }
    }
}

// Ubah Laporan (yang punya koordinat) jadi Hotspot supaya bisa digambar sebagai titik di peta.
private fun Report.toMapHotspot(markerId: Int): Hotspot? {
    val la = lat ?: return null
    val ln = lng ?: return null
    return Hotspot(
        id = markerId,
        risk = risk,
        score = score,
        count = 1,
        xFrac = 0f, yFrac = 0f,
        lat = la, lng = ln,
        name = address ?: "Laporan $code",
        kel = kelurahan,
        kec = kecamatan,
        cat = category,
        status = status,
        src = HotspotSource.Warga,
        dist = "",
        when_ = updatedAt ?: createdAt,
    )
}

data class AreaSummary(val kecamatan: String, val activeCount: Int, val level: RiskLevel, val score: Int)

@Composable
fun AreaKamuCard(summary: AreaSummary) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
            .clip(Radius.lg)
            .background(Color.White.copy(alpha = 0.95f))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(PrimarySoft),
            contentAlignment = Alignment.Center,
        ) {
            Icon(AlirinIcons.pin, null, tint = Primary, modifier = Modifier.size(22.dp))
        }
        Column(Modifier.weight(1f)) {
            Text("Area kamu · radius 1 km", style = AlirinText.caption)
            Text(
                text = "${summary.kecamatan} · ${summary.activeCount} titik aktif",
                fontWeight = FontWeight.W700,
                fontSize = 14.5.sp,
                color = Ink,
                letterSpacing = (-0.15).sp,
            )
        }
        RiskPill(level = summary.level, score = summary.score)
    }
}

@Composable
private fun FilterChips(value: HotspotSource?, onChange: (HotspotSource?) -> Unit) {
    val items = listOf<Triple<HotspotSource?, String, ImageVector>>(
        Triple(null,                       "Semua",    AlirinIcons.layers),
        Triple(HotspotSource.Warga,        "Warga",    AlirinIcons.users),
        Triple(HotspotSource.Historis,     "Historis", AlirinIcons.history),
        Triple(HotspotSource.Cuaca,        "Cuaca",    AlirinIcons.cloud),
        Triple(HotspotSource.Iot,          "Sensor",   AlirinIcons.sensor),
    )
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(items) { (src, label, icon) ->
            val active = value == src
            Row(
                Modifier
                    .clip(Radius.pill)
                    .background(if (active) Ink else Color.White.copy(alpha = 0.85f))
                    .clickable { onChange(src) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(icon, null, tint = if (active) Color.White else Ink2, modifier = Modifier.size(14.dp))
                Text(label, color = if (active) Color.White else Ink2, fontSize = 12.sp, fontWeight = FontWeight.W600)
            }
        }
    }
}

@Composable
private fun MapButton(icon: ImageVector, primary: Boolean = false, onClick: () -> Unit = {}) {
    val bg = if (primary) Primary else Color.White.copy(alpha = 0.95f)
    val tint = if (primary) Color.White else Ink
    Box(
        Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .border(0.5.dp, Hairline, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun HotspotSheetContent(
    hotspot: Hotspot,
    onClose: () -> Unit,
    onAddHere: () -> Unit,
) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    Column(
        Modifier
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, bottom = 24.dp, top = 4.dp)
    ) {

        Box(
            Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 12.dp)
                .size(width = 36.dp, height = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Hairline2)
        )

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(Modifier.weight(1f)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    RiskPill(level = hotspot.risk, score = hotspot.score)
                    Pill(label = hotspot.src.label, bg = Surface2, ink = Ink3)
                }
                Text(hotspot.name, style = AlirinText.h3, modifier = Modifier.padding(top = 6.dp))
                Text(
                    "${hotspot.kel} · ${hotspot.kec} · ${hotspot.dist}",
                    style = AlirinText.caption,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            AlirinIconBubble(
                icon = AlirinIcons.close,
                bg = Surface2,
                onClick = onClose,
                size = 36,
            )
        }

        if (hotspot.src == HotspotSource.Warga && hotspot.count > 0) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .clip(Radius.md)
                    .background(PrimarySoft)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(AlirinIcons.users, null, tint = PrimaryInk, modifier = Modifier.size(18.dp))
                Text(
                    "${hotspot.count} warga melaporkan · diverifikasi gotong-royong",
                    color = PrimaryInk,
                    fontWeight = FontWeight.W600,
                    fontSize = 13.sp,
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            KV(label = "Kategori", value = hotspot.cat, modifier = Modifier.weight(1f))
            Box(Modifier.weight(1f)) {
                Column {
                    Text("Status", style = AlirinText.caption, modifier = Modifier.padding(bottom = 4.dp))
                    StatusPill(status = hotspot.status)
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            KV(label = "Laporan", value = "${hotspot.count} dari warga", modifier = Modifier.weight(1f))
            KV(label = "Update", value = hotspot.when_, modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(14.dp))
        AlirinPlaceholder(label = "foto laporan terkini", height = 120, tone = PlaceholderTone.Indigo, shape = Radius.md)

        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AlirinButton(
                label = "Bagikan",
                leadingIcon = AlirinIcons.share,
                variant = BtnVariant.Soft,
                onClick = {
                    ctx.shareText(
                        "Titik rawan ALIRIN: ${hotspot.name}\n" +
                            "${hotspot.kel}, ${hotspot.kec} · ${hotspot.risk.label} (skor ${hotspot.score})",
                    )
                },
                modifier = Modifier.weight(1f),
            )
            AlirinButton(
                label = "Tambah laporan di sini",
                trailingIcon = AlirinIcons.arrowRight,
                onClick = onAddHere,
                modifier = Modifier.weight(2f),
            )
        }
    }
}

@Composable
private fun KV(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, style = AlirinText.caption, modifier = Modifier.padding(bottom = 4.dp))
        Text(value, fontWeight = FontWeight.W600, fontSize = 13.5.sp, color = Ink, letterSpacing = (-0.13).sp)
    }
}
