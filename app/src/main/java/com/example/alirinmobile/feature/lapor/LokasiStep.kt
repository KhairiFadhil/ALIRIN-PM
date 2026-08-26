package com.example.alirinmobile.feature.lapor

import android.Manifest
import android.content.Context
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.alirinmobile.feature.AlirinViewModelFactory
import com.example.alirinmobile.AlirinApplication
import com.example.alirinmobile.feature.LocationViewModel
import androidx.compose.runtime.mutableIntStateOf
import com.example.alirinmobile.feature.peta.LocationPickerMap
import com.example.alirinmobile.ui.components.*
import com.example.alirinmobile.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
fun LokasiStep(
    form: LaporForm,
    onUpdate: (LaporForm) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    step: Int,
    total: Int,
) {
    val locVm: LocationViewModel = viewModel(factory = AlirinViewModelFactory.Factory)
    val ctx = LocalContext.current
    var fetching by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var recenterKey by remember { mutableIntStateOf(0) }
    var showKecamatanPicker by remember { mutableStateOf(false) }
    var showKelurahanPicker by remember { mutableStateOf(false) }

    val kelurahanRepo = remember { AlirinApplication.get().kelurahanRepository }
    val areaValid = kelurahanRepo.isValidArea(form.kelurahan, form.kecamatan)

    val currentForm by rememberUpdatedState(form)
    val currentOnUpdate by rememberUpdatedState(onUpdate)
    LaunchedEffect(form.lat, form.lng) {
        val la = form.lat
        val ln = form.lng
        if (la != null && ln != null) {
            delay(500)
            // Sumber utama: kelurahan TERDEKAT dari titik, dihitung dari
            // koordinat pusat kelurahan. Deterministik dan selalu sah, sehingga
            // wilayah otomatis mengikuti lokasi -- inti keluhan sebelumnya.
            val nearest = kelurahanRepo.nearest(la, ln)
            // Cadangan: hasil reverse-geocode, hanya bila cocok master. Dipakai
            // saat koordinat kelurahan belum tersedia (aset lama).
            val geocoded = withContext(Dispatchers.IO) { reverseGeocode(ctx, la, ln) }
                .let { kelurahanRepo.matchArea(it?.first, it?.second) }
            val pick = nearest?.let { it.kecamatan to it.kelurahan } ?: geocoded
            if (pick != null) {
                currentOnUpdate(
                    currentForm.copy(kecamatan = pick.first, kelurahan = pick.second)
                )
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            fetching = true
            locVm.fetchOnce { loc ->
                fetching = false
                if (loc != null) {
                    onUpdate(form.copy(lat = loc.lat, lng = loc.lng, accuracyMeters = loc.accuracyMeters))
                    recenterKey++
                    error = null
                } else error = "Lokasi tidak tersedia. Coba di tempat terbuka."
            }
        } else {
            error = "Akses lokasi ditolak. Kamu masih bisa geser peta manual."
        }
    }

    fun requestOrFetch() {
        if (locVm.hasPermission()) {
            fetching = true
            locVm.fetchOnce { loc ->
                fetching = false
                if (loc != null) {
                    onUpdate(form.copy(lat = loc.lat, lng = loc.lng, accuracyMeters = loc.accuracyMeters))
                    recenterKey++
                    error = null
                } else error = "Lokasi tidak tersedia. Coba di tempat terbuka."
            }
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Column(Modifier.fillMaxSize().background(Bg)) {
        StepHeader(
            step = step,
            total = total,
            onBack = onBack,
            title = "Di mana lokasinya?",
            subtitle = "Aktifkan GPS atau geser pin di peta. Wilayah terisi otomatis bila terdeteksi, dan bisa dipilih sendiri."
        )
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {

            Box(
                Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .shadow(2.dp, Radius.lg)
                    .clip(Radius.lg)
            ) {
                LocationPickerMap(
                    lat = form.lat,
                    lng = form.lng,
                    recenterKey = recenterKey,
                    onCenterChanged = { la, ln ->
                        onUpdate(form.copy(lat = la, lng = ln, accuracyMeters = null))
                    },
                    modifier = Modifier.fillMaxSize(),
                )

                Box(Modifier.align(Alignment.Center).offset(y = (-22).dp).size(40.dp)) {
                    PinShape(color = Primary)
                }

                Row(
                    Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .clip(Radius.pill)
                        .background(Color.White.copy(alpha = 0.95f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(AlirinIcons.pin, null, tint = Ink, modifier = Modifier.size(12.dp))
                    Text("Geser peta untuk atur titik", fontSize = 12.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.W600, color = Ink)
                }
            }
            Spacer(Modifier.height(14.dp))

            AlirinButton(
                label = if (fetching) "Mengambil lokasi..." else "Gunakan lokasi saya",
                onClick = { if (!fetching) requestOrFetch() },
                leadingIcon = AlirinIcons.pin,
                block = true,
                enabled = !fetching,
            )
            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(error!!, color = RiskKritisDot, fontSize = 12.5.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.W500)
            }
            Spacer(Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val coordsLabel = if (form.lat != null && form.lng != null) {
                    val accuracy = form.accuracyMeters?.let { " · ±${it.toInt()} m" }.orEmpty()
                    "%.5f, %.5f%s".format(form.lat, form.lng, accuracy)
                } else "Belum dipilih · geser peta atau aktifkan GPS"
                ReadOnlyFieldBordered(label = "Latitude · Longitude", value = coordsLabel, mono = true)

                // Wilayah kini bisa dipilih sendiri, tidak lagi hanya hasil
                // tebakan Geocoder. Sebelumnya kolom ini read-only, sehingga
                // ketika Geocoder mengembalikan nama jalan atau wilayah di luar
                // master, laporan ditolak saat dikirim dan pengguna tidak punya
                // cara memperbaikinya.
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PickerFieldBordered(
                        label = "Kecamatan",
                        value = form.kecamatan.ifBlank { "Pilih kecamatan" },
                        filled = form.kecamatan.isNotBlank(),
                        onClick = { showKecamatanPicker = true },
                        modifier = Modifier.weight(1f),
                    )
                    PickerFieldBordered(
                        label = "Kelurahan",
                        value = form.kelurahan.ifBlank {
                            if (form.kecamatan.isBlank()) "Pilih kecamatan dulu" else "Pilih kelurahan"
                        },
                        filled = form.kelurahan.isNotBlank(),
                        onClick = { if (form.kecamatan.isNotBlank()) showKelurahanPicker = true },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (!areaValid) {
                    Text(
                        if (form.kecamatan.isBlank() && form.kelurahan.isBlank()) {
                            "Wilayah belum terisi otomatis. Ketuk untuk memilih kecamatan dan kelurahan."
                        } else {
                            "Wilayah belum cocok dengan daftar resmi. Ketuk untuk memilih yang benar."
                        },
                        color = RiskKritisDot,
                        fontSize = 12.5.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.W500,
                    )
                }
                TextField(
                    value = form.alamat,
                    onValueChange = { onUpdate(form.copy(alamat = it)) },
                    placeholder = "Patokan / nama jalan (opsional)",
                    leadingIcon = AlirinIcons.pin,
                )
            }
            Spacer(Modifier.height(80.dp))
        }
        FlowFooter(
            primary = "Lanjut",
            enabled = areaValid && form.lat != null && form.lng != null,
            onPrimary = onNext,
            info = {
                Icon(AlirinIcons.pin, null, tint = Primary, modifier = Modifier.size(12.dp))
                val infoText = when {
                    form.lat != null && form.accuracyMeters != null ->
                        "Lokasi terdeteksi: ±${form.accuracyMeters.toInt()} m"
                    form.lat != null -> "Lokasi terdeteksi"
                    else -> "Lokasi default — tekan tombol GPS"
                }
                Text(infoText, color = Ink3, fontSize = 12.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.W500)
            },
        )
    }

    if (showKecamatanPicker) {
        AreaPickerSheet(
            title = "Pilih kecamatan",
            options = kelurahanRepo.kecamatanList,
            selected = form.kecamatan,
            onPick = { picked ->
                // Mengganti kecamatan membuat kelurahan lama tidak lagi sah,
                // jadi langsung dikosongkan dan pemilih kelurahan dibuka.
                onUpdate(form.copy(kecamatan = picked, kelurahan = ""))
                showKecamatanPicker = false
                showKelurahanPicker = true
            },
            onDismiss = { showKecamatanPicker = false },
        )
    }

    if (showKelurahanPicker && form.kecamatan.isNotBlank()) {
        AreaPickerSheet(
            title = "Pilih kelurahan di ${form.kecamatan}",
            options = kelurahanRepo.kelurahanOf(form.kecamatan),
            selected = form.kelurahan,
            onPick = { picked ->
                onUpdate(form.copy(kelurahan = picked))
                showKelurahanPicker = false
            },
            onDismiss = { showKelurahanPicker = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AreaPickerSheet(
    title: String,
    options: List<String>,
    selected: String,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = Radius.sheet,
        containerColor = Surface,
    ) {
        Column(Modifier.fillMaxWidth().padding(bottom = 28.dp)) {
            Text(
                title,
                style = AlirinText.h3,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 12.dp),
            )
            LazyColumn(Modifier.fillMaxWidth().heightIn(max = 420.dp)) {
                items(options) { option ->
                    val active = option.equals(selected, ignoreCase = true)
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onPick(option) }
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            option,
                            style = AlirinText.body.copy(
                                color = if (active) Primary else Ink,
                                fontWeight = if (active) androidx.compose.ui.text.font.FontWeight.W700
                                             else androidx.compose.ui.text.font.FontWeight.W500,
                            ),
                        )
                        if (active) Icon(AlirinIcons.check, null, tint = Primary, modifier = Modifier.size(18.dp))
                    }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(Hairline))
                }
            }
        }
    }
}

@Composable
private fun PickerFieldBordered(
    label: String,
    value: String,
    filled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .clip(Radius.md)
            .background(Surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(label, style = AlirinText.caption)
        Row(
            Modifier.fillMaxWidth().padding(top = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                value,
                style = AlirinText.body.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.W600,
                    color = if (filled) Ink else Muted,
                ),
            )
            Icon(AlirinIcons.chevronDown, null, tint = Muted, modifier = Modifier.size(16.dp))
        }
    }
}

private fun reverseGeocode(ctx: Context, lat: Double, lng: Double): Pair<String?, String?>? =
    runCatching {
        @Suppress("DEPRECATION")
        val res = Geocoder(ctx, Locale("id")).getFromLocation(lat, lng, 1)
        val a = res?.firstOrNull() ?: return null
        // subLocality saja untuk kelurahan. Versi lama mencadangkannya ke
        // thoroughfare (nama jalan) dan featureName, yang praktis tidak pernah
        // berupa nama kelurahan sehingga selalu gagal validasi.
        val kecamatan = a.subAdminArea ?: a.locality
        val kelurahan = a.subLocality ?: a.locality
        kecamatan to kelurahan
    }.getOrNull()

@Composable
private fun ReadOnlyFieldBordered(label: String, value: String, mono: Boolean = false, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(Radius.md)
            .background(Surface)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(label, style = AlirinText.caption)
        Text(
            value,
            modifier = Modifier.padding(top = 2.dp),
            style = if (mono) AlirinText.mono.copy(fontSize = 14.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.W600, color = Ink)
                    else AlirinText.body.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.W600),
        )
    }
}

@Composable
private fun PinShape(color: Color) {
    androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val path = androidx.compose.ui.graphics.Path().apply {

            moveTo(w * 0.5f, 0f)
            cubicTo(w * 0.22f, 0f, 0f, w * 0.2f, 0f, w * 0.5f)
            cubicTo(0f, h * 0.7f, w * 0.5f, h, w * 0.5f, h)
            cubicTo(w * 0.5f, h, w, h * 0.7f, w, w * 0.5f)
            cubicTo(w, w * 0.2f, w * 0.78f, 0f, w * 0.5f, 0f)
            close()
        }
        drawPath(path, color = color)
        drawCircle(
            color = Color.White,
            radius = w * 0.15f,
            center = androidx.compose.ui.geometry.Offset(w * 0.5f, w * 0.4f),
        )
    }
}
