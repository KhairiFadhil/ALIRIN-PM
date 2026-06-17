package com.example.alirinmobile.feature.lapor

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.alirinmobile.feature.AlirinViewModelFactory
import com.example.alirinmobile.feature.LocationViewModel
import com.example.alirinmobile.feature.WeatherViewModel
import com.example.alirinmobile.feature.peta.MapBackground
import com.example.alirinmobile.feature.peta.MapStyle
import com.example.alirinmobile.ui.components.*
import com.example.alirinmobile.ui.theme.*

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
    val weatherVm: WeatherViewModel = viewModel(factory = AlirinViewModelFactory.Factory)
    val selectedKel by weatherVm.selected.collectAsStateWithLifecycle()
    var fetching by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(selectedKel) {
        if (form.kecamatan.isBlank() && selectedKel != null) {
            onUpdate(form.copy(kecamatan = selectedKel!!.kecamatan, kelurahan = selectedKel!!.kelurahan))
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
                    error = null
                } else error = "Lokasi tidak tersedia. Coba di tempat terbuka."
            }
        } else {
            error = "Akses lokasi ditolak. Kamu masih bisa pin manual."
        }
    }

    fun requestOrFetch() {
        if (locVm.hasPermission()) {
            fetching = true
            locVm.fetchOnce { loc ->
                fetching = false
                if (loc != null) {
                    onUpdate(form.copy(lat = loc.lat, lng = loc.lng, accuracyMeters = loc.accuracyMeters))
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
            subtitle = "Aktifkan GPS atau pin manual di peta. Kecamatan & kelurahan akan terisi otomatis."
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
                MapBackground(style = MapStyle.Light)

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
                    Text("Geser pin untuk akurasi", fontSize = 12.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.W600, color = Ink)
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
                } else "-5.39812, 105.26012 · belum di-GPS"
                ReadOnlyFieldBordered(label = "Latitude · Longitude", value = coordsLabel, mono = true)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ReadOnlyFieldBordered(
                        label = "Kecamatan",
                        value = form.kecamatan.ifBlank { selectedKel?.kecamatan ?: "—" },
                        modifier = Modifier.weight(1f),
                    )
                    ReadOnlyFieldBordered(
                        label = "Kelurahan",
                        value = form.kelurahan.ifBlank { selectedKel?.kelurahan ?: "—" },
                        modifier = Modifier.weight(1f),
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
}

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
