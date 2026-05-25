package com.example.alirinmobile.feature.lapor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.alirinmobile.data.Photo
import com.example.alirinmobile.data.PhotoKind
import com.example.alirinmobile.data.PhotoStore
import com.example.alirinmobile.ui.components.*
import com.example.alirinmobile.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun FotoStep(
    form: LaporForm,
    onUpdate: (LaporForm) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
    step: Int,
    total: Int,
) {
    val photos = form.photos
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    fun appendPhoto(kind: PhotoKind, uri: String?) {
        if (photos.size >= 3) return
        val now = System.currentTimeMillis()
        onUpdate(form.copy(photos = photos + Photo(now, kind, now, uri)))
    }
    fun remove(id: Long) {
        photos.firstOrNull { it.id == id }?.uri?.let { path ->
            runCatching { File(path).delete() }
        }
        onUpdate(form.copy(photos = photos.filterNot { it.id == id }))
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap ?: return@rememberLauncherForActivityResult
        scope.launch {
            val path = withContext(Dispatchers.IO) {
                PhotoStore.saveCameraBitmap(ctx, bitmap, lat = form.lat, lng = form.lng)
            }
            appendPhoto(PhotoKind.Camera, path)
        }
    }
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            val path = withContext(Dispatchers.IO) {
                runCatching { PhotoStore.saveGalleryUri(ctx, uri) }.getOrNull()
            }
            appendPhoto(PhotoKind.Gallery, path)
        }
    }

    fun addCamera() { if (photos.size < 3) cameraLauncher.launch(null) }
    fun addGallery() {
        if (photos.size < 3) galleryLauncher.launch(
            androidx.activity.result.PickVisualMediaRequest(
                ActivityResultContracts.PickVisualMedia.ImageOnly
            )
        )
    }

    Column(Modifier.fillMaxSize().background(Bg)) {
        StepHeader(step, total, onBack,
            title = "Tambah bukti foto",
            subtitle = "1–3 foto. Foto kamera otomatis di-watermark (lokasi + waktu) supaya bisa diverifikasi.",
        )
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            // 3-up grid (square-ish)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { i ->
                    val photo = photos.getOrNull(i)
                    Box(Modifier.weight(1f).aspectRatio(3f / 4f)) {
                        if (photo != null) PhotoSlot(photo = photo, onRemove = { remove(photo.id) })
                        else EmptyPhotoSlot(index = i, onClick = { addCamera() })
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                AlirinButton(
                    label = "Kamera",
                    onClick = { addCamera() },
                    leadingIcon = AlirinIcons.camera,
                    modifier = Modifier.weight(1f),
                )
                AlirinButton(
                    label = "Galeri",
                    onClick = { addGallery() },
                    leadingIcon = AlirinIcons.image,
                    variant = BtnVariant.Soft,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(20.dp))

            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(Radius.md)
                    .background(PrimarySoft)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(AlirinIcons.shield, null, tint = PrimaryInk, modifier = Modifier.size(18.dp))
                Column {
                    Text(
                        "Auto-watermark: lat/long & timestamp ditambahkan ke foto.",
                        color = PrimaryInk, fontSize = 12.5.sp, fontWeight = FontWeight.W600,
                    )
                    Text(
                        "Wajah orang lewat otomatis di-blur.",
                        color = PrimaryInk, fontSize = 12.5.sp, lineHeight = 18.sp,
                    )
                }
            }
            Spacer(Modifier.height(20.dp))

            SubLabelWithOptional("Kontak", optional = true)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = form.nama,
                    onValueChange = { onUpdate(form.copy(nama = it)) },
                    placeholder = "Nama (opsional)",
                )
                TextField(
                    value = form.kontak,
                    onValueChange = { onUpdate(form.copy(kontak = it)) },
                    placeholder = "No. HP / email (opsional)",
                )
            }
            Text(
                "Tidak wajib — admin akan menghubungi hanya jika perlu klarifikasi.",
                style = AlirinText.caption,
                lineHeight = 17.sp,
                modifier = Modifier.padding(top = 8.dp),
            )
            Spacer(Modifier.height(60.dp))
        }
        FlowFooter(
            primary = "Kirim laporan",
            onPrimary = onNext,
            enabled = photos.isNotEmpty(),
            info = {
                Text("${photos.size}/3 foto · siap dikirim", color = Ink3, fontSize = 12.sp, fontWeight = FontWeight.W500)
            },
        )
    }
}

@Composable
private fun EmptyPhotoSlot(index: Int, onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .clip(Radius.md)
            .background(Surface2)
            .border(1.5.dp, Hairline2, Radius.md)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(AlirinIcons.plus, null, tint = Muted, modifier = Modifier.size(20.dp))
            Text("${index + 1}/3", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.W600)
        }
    }
}

@Composable
private fun PhotoSlot(photo: Photo, onRemove: () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .clip(Radius.md)
            .background(Surface),
    ) {
        if (photo.uri != null) {
            AsyncImage(
                model = File(photo.uri),
                contentDescription = null,
                modifier = Modifier.matchParentSize().clip(Radius.md),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            )
        } else {
            AlirinPlaceholder(
                label = if (photo.kind == PhotoKind.Camera) "foto kamera" else "foto galeri",
                height = null,
                tone = if (photo.kind == PhotoKind.Camera) PlaceholderTone.Indigo else PlaceholderTone.Warm,
                shape = Radius.md,
            )
        }
        // Gallery warning (camera photos already carry their watermark in the bitmap)
        if (photo.kind == PhotoKind.Gallery) {
            Text(
                "tidak diverifikasi lokasi",
                color = Color.White,
                fontSize = 8.sp,
                fontWeight = FontWeight.W600,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Amber2)
                    .padding(horizontal = 5.dp, vertical = 2.dp),
            )
        }
        // Remove button
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .padding(5.dp)
                .size(22.dp)
                .clip(CircleShape)
                .background(Color(0xD90E1130))
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center,
        ) {
            Icon(AlirinIcons.close, null, tint = Color.White, modifier = Modifier.size(12.dp))
        }
    }
}
