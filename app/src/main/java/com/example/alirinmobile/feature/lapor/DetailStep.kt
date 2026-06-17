package com.example.alirinmobile.feature.lapor

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alirinmobile.data.Kategoris
import com.example.alirinmobile.data.ReportMode
import com.example.alirinmobile.data.Severities
import com.example.alirinmobile.ui.components.AlirinIcons
import com.example.alirinmobile.ui.theme.*

@Composable
fun DetailStep(
    form: LaporForm,
    onUpdate: (LaporForm) -> Unit,
    mode: ReportMode,
    onNext: () -> Unit,
    onBack: () -> Unit,
    step: Int,
    total: Int,
) {
    val required = mode == ReportMode.Lengkap
    Column(Modifier.fillMaxSize().background(Bg)) {
        StepHeader(step, total, onBack,
            title = "Apa masalahnya?",
            subtitle = "Pilih satu kategori dan tingkat keparahan.",
        )
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        ) {
            SubLabel("Kategori")

            val rows = Kategoris.chunked(2)
            rows.forEach { row ->
                Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { k ->
                        val active = form.kategori == k.id
                        Box(
                            Modifier
                                .weight(1f)
                                .clip(Radius.md)
                                .background(if (active) Ink else Surface)
                                .border(1.dp, if (active) Ink else Hairline, Radius.md)
                                .clickable { onUpdate(form.copy(kategori = k.id)) }
                                .padding(12.dp),
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (active) Color.White.copy(alpha = 0.12f) else Surface2),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        AlirinIcons.byName(k.iconName), null,
                                        tint = if (active) Color.White else Ink2,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                                Text(
                                    k.label,
                                    fontWeight = FontWeight.W700,
                                    fontSize = 13.5.sp,
                                    color = if (active) Color.White else Ink,
                                    letterSpacing = (-0.13).sp,
                                    lineHeight = 16.sp,
                                )
                                Text(
                                    k.desc,
                                    fontSize = 11.sp,
                                    color = if (active) Color.White.copy(alpha = 0.65f) else Ink3,
                                    lineHeight = 14.sp,
                                )
                            }
                        }
                    }
                    if (row.size == 1) Spacer(Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(12.dp))

            SubLabel("Tingkat keparahan")
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Severities.forEach { s ->
                    val active = form.severity == s.id
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(Radius.md)
                            .background(if (active) s.bg else Surface)
                            .border(
                                1.dp,
                                if (active) s.ink.copy(alpha = 0.2f) else Hairline,
                                Radius.md,
                            )
                            .clickable { onUpdate(form.copy(severity = s.id)) }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .border(2.dp, if (active) s.ink else Hairline2, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (active) {
                                Box(
                                    Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(s.ink)
                                )
                            }
                        }
                        Column(Modifier.weight(1f)) {
                            Text(s.label, fontWeight = FontWeight.W700, fontSize = 14.sp, color = if (active) s.ink else Ink)
                            Text(
                                s.desc,
                                fontSize = 12.sp,
                                color = if (active) s.ink.copy(alpha = 0.7f) else Ink3,
                                modifier = Modifier.padding(top = 1.dp),
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))

            SubLabelWithOptional("Deskripsi", optional = !required)
            TextField(
                value = form.deskripsi,
                onValueChange = { onUpdate(form.copy(deskripsi = it)) },
                placeholder = if (required) "Ceritakan singkat: lokasi pasti, panjang genangan, dampaknya..."
                              else "Opsional: tambahkan info singkat...",
                singleLine = false,
                minHeight = 110,
            )
            if (required) {
                Text(
                    "${form.deskripsi.length} / min 10 karakter",
                    style = AlirinText.caption,
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                )
            }
            Spacer(Modifier.height(60.dp))
        }
        val canSubmit = form.kategori.isNotEmpty() && form.severity.isNotEmpty() &&
                        (!required || form.deskripsi.length >= 10)
        FlowFooter(
            primary = if (mode == ReportMode.Lengkap) "Lanjut ke foto" else "Kirim laporan",
            onPrimary = onNext,
            enabled = canSubmit,
            info = {
                if (form.kategori.isNotEmpty() && form.severity.isNotEmpty()) {
                    val k = Kategoris.find { it.id == form.kategori }?.label.orEmpty()
                    val s = Severities.find { it.id == form.severity }?.label.orEmpty()
                    Text("$k · $s", color = Ink3, fontSize = 12.sp, fontWeight = FontWeight.W500)
                }
            },
        )
    }
}
