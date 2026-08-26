package com.example.alirinmobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alirinmobile.data.RiskBreakdownItem
import com.example.alirinmobile.ui.theme.Hairline
import com.example.alirinmobile.ui.theme.Ink
import com.example.alirinmobile.ui.theme.Ink2
import com.example.alirinmobile.ui.theme.Muted
import com.example.alirinmobile.ui.theme.Primary
import com.example.alirinmobile.ui.theme.Radius
import com.example.alirinmobile.ui.theme.RiskKritisDot
import com.example.alirinmobile.ui.theme.RiskNormalDot
import com.example.alirinmobile.ui.theme.RiskWaspadaDot
import com.example.alirinmobile.ui.theme.Surface2

// P-2 - Rincian skor yang bisa dibaca warga dan petugas.
//
// Sumbernya kolom risk_breakdown yang datang dari basis data, bukan hitungan
// ulang di layar. Poin tiap faktor dijamin berjumlah sama dengan skor akhir
// (lihat RiskEngine.apportion dan alirin_apportion), jadi angkanya boleh
// dijumlahkan pengguna tanpa menemukan selisih.

private fun fillColor(percentage: Int): Color = when {
    percentage >= 70 -> RiskKritisDot
    percentage >= 40 -> RiskWaspadaDot
    else -> RiskNormalDot
}

@Composable
fun RiskBreakdownCard(
    items: List<RiskBreakdownItem>,
    modifier: Modifier = Modifier,
    title: String = "Kenapa skornya segini",
) {
    if (items.isEmpty()) {
        Text(
            text = "Rincian faktor belum tersedia untuk laporan ini.",
            color = Muted,
            fontSize = 12.sp,
            modifier = modifier,
        )
        return
    }

    val weighted = items.filter { it.weight > 0 }
    val unweighted = items.filter { it.weight <= 0 }
    val total = weighted.sumOf { it.points }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title.uppercase(),
                color = Muted,
                fontSize = 11.sp,
                fontWeight = FontWeight.W600,
                letterSpacing = 0.8.sp,
            )
            // Dijumlahkan dari baris di bawah, bukan disalin dari skor,
            // supaya selisih apa pun langsung terlihat.
            Text(
                text = "$total / 100",
                color = Primary,
                fontSize = 13.sp,
                fontWeight = FontWeight.W700,
            )
        }

        weighted.forEach { item ->
            val fill = if (item.weight > 0) {
                (item.points * 100 / item.weight).coerceIn(0, 100)
            } else {
                0
            }

            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = item.label,
                        color = Ink2,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.W600,
                    )
                    Row {
                        Text(
                            text = "${item.points}",
                            color = Ink,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.W700,
                        )
                        Text(
                            text = "/${item.weight}",
                            color = Muted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.W400,
                        )
                    }
                }

                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(Radius.pill)
                        .background(Surface2)
                ) {
                    if (fill > 0) {
                        Spacer(
                            Modifier
                                .fillMaxWidth(fill / 100f)
                                .height(5.dp)
                                .clip(Radius.pill)
                                .background(fillColor(fill))
                        )
                    }
                }

                item.detail?.let {
                    Text(text = it, color = Muted, fontSize = 11.5.sp, lineHeight = 16.sp)
                }
            }
        }

        if (unweighted.isNotEmpty()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Hairline)
            ) {}

            Text(
                text = "DICATAT, BELUM DIBOBOT",
                color = Muted,
                fontSize = 11.sp,
                fontWeight = FontWeight.W600,
                letterSpacing = 0.8.sp,
            )

            unweighted.forEach { item ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = item.label,
                        color = Ink2,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.W600,
                    )
                    Text(
                        text = item.detail.orEmpty(),
                        color = Muted,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}
