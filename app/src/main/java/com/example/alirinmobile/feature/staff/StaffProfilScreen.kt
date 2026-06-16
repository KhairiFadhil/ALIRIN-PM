package com.example.alirinmobile.feature.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alirinmobile.data.auth.AuthSession
import com.example.alirinmobile.ui.components.*
import com.example.alirinmobile.ui.theme.*

@Composable
fun StaffProfilScreen(actor: AuthSession?, onLogout: () -> Unit) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    Column(Modifier.fillMaxSize().background(Bg)) {
        AlirinTopBar(title = "Profil")
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AlirinCard(
                modifier = Modifier.fillMaxWidth(),
                padding = PaddingValues(20.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Avatar(seed = (actor?.userId ?: 3) % 6, size = 56, label = (actor?.displayName?.take(2) ?: "ST").uppercase())
                    Column(Modifier.weight(1f)) {
                        Text(
                            actor?.displayName ?: "Staff",
                            fontWeight = FontWeight.W700,
                            fontSize = 17.sp,
                            color = Ink,
                            letterSpacing = (-0.25).sp,
                        )
                        Text(
                            "Staff Validator · ID ${actor?.username ?: "—"} · Kemiling",
                            style = AlirinText.caption,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                }
            }

            // Settings list
            AlirinCard(modifier = Modifier.fillMaxWidth(), padding = PaddingValues(0.dp)) {
                Column {
                    val items = listOf(
                        "Notifikasi"           to "Aktif",
                        "Wilayah penugasan"    to "Kemiling, Way Halim",
                        "Bahasa"               to "Bahasa Indonesia",
                        "Tampilan"             to "Otomatis",
                    )
                    items.forEachIndexed { i, (label, value) ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { ctx.toast("$label: $value") }
                                .padding(horizontal = 18.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(label, fontWeight = FontWeight.W600, fontSize = 14.sp, color = Ink)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(value, style = AlirinText.caption)
                                Icon(AlirinIcons.chevronRight, null, tint = Muted, modifier = Modifier.size(16.dp))
                            }
                        }
                        if (i < items.lastIndex) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 18.dp)
                                    .height(1.dp)
                                    .background(Hairline)
                            )
                        }
                    }
                }
            }

            // Ganti peran
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(Radius.lg)
                    .background(Surface)
                    .border(1.dp, Hairline, Radius.lg)
                    .clickable(onClick = onLogout)
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(AlirinIcons.arrowLeft, null, tint = Ink3, modifier = Modifier.size(20.dp))
                Column(Modifier.weight(1f)) {
                    Text("Ganti peran", fontWeight = FontWeight.W600, fontSize = 14.sp, color = Ink)
                    Text(
                        "Kembali ke pemilihan Warga / Staff",
                        style = AlirinText.caption,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "ALIRIN · build 2026.05.25",
                style = AlirinText.caption,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }
}
