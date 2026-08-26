package com.example.alirinmobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alirinmobile.data.Report
import com.example.alirinmobile.ui.theme.Ink
import com.example.alirinmobile.ui.theme.Ink2
import com.example.alirinmobile.ui.theme.Muted
import com.example.alirinmobile.ui.theme.Primary
import com.example.alirinmobile.ui.theme.Radius
import com.example.alirinmobile.ui.theme.Surface2

// P-1 - Penilaian AI ditampilkan BERDAMPINGAN dengan baseline, bukan
// menggantikannya.
//
// Proposal 4.3.4 menjanjikan AI "dibandingkan dengan baseline serta verifikasi
// lapangan". Menampilkan satu angka saja akan menghapus perbandingan itu, dan
// pengguna tidak lagi bisa membedakan mana yang berasal dari aturan yang bisa
// dilacak dan mana yang berasal dari model. Selisihnya justru bagian yang
// menarik: ia menjadi bahan evaluasi akurasi yang dijanjikan Proposal 4.4.
//
// Urutan penanganan tetap memakai skor baseline.

@Composable
fun AiAssessmentCard(report: Report, modifier: Modifier = Modifier) {
    val ai = report.aiRiskScore ?: return
    val selisih = ai - report.score
    val arah = when {
        selisih > 0 -> "lebih tinggi $selisih"
        selisih < 0 -> "lebih rendah ${-selisih}"
        else -> "sama"
    }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "PEMBANDING AI",
                color = Muted,
                fontSize = 11.sp,
                fontWeight = FontWeight.W600,
                letterSpacing = 0.8.sp,
            )
            report.aiModel?.takeIf { it.isNotBlank() }?.let {
                Text(text = it, color = Muted, fontSize = 10.sp)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ScoreCell("${report.score}", "Baseline (dipakai)", Ink, Modifier.weight(1f))
            ScoreCell("$ai", "AI ($arah)", Primary, Modifier.weight(1f))
        }

        report.aiRiskReason?.takeIf { it.isNotBlank() }?.let {
            Text(text = it, color = Ink2, fontSize = 12.5.sp, lineHeight = 18.sp)
        }

        report.aiRecommendations.forEach {
            Text(text = "· $it", color = Muted, fontSize = 12.sp, lineHeight = 17.sp)
        }

        Text(
            text = "Urutan penanganan memakai skor baseline. Angka AI ditampilkan " +
                "untuk dibandingkan, bukan untuk menggantikan.",
            color = Muted,
            fontSize = 10.5.sp,
            lineHeight = 15.sp,
        )
    }
}

@Composable
private fun ScoreCell(value: String, label: String, tint: androidx.compose.ui.graphics.Color, modifier: Modifier) {
    Column(
        modifier
            .clip(Radius.md)
            .background(Surface2)
            .padding(vertical = 12.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text(text = value, color = tint, fontSize = 22.sp, fontWeight = FontWeight.W700)
        Text(text = label, color = Muted, fontSize = 10.5.sp)
    }
}
