package com.example.alirinmobile.feature.lapor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.alirinmobile.data.Photo
import com.example.alirinmobile.data.ReportMode

enum class LaporStep { Choice, Lokasi, Detail, Foto, Success }

data class LaporForm(
    val kategori: String = "",
    val severity: String = "",
    val deskripsi: String = "",
    val alamat: String = "",
    val photos: List<Photo> = emptyList(),
    val nama: String = "",
    val kontak: String = "",
    /** Real GPS coords once "Gunakan lokasi saya" succeeded. */
    val lat: Double? = null,
    val lng: Double? = null,
    val accuracyMeters: Float? = null,
)

class LaporFlowState(
    initialStep: LaporStep = LaporStep.Choice,
    initialMode: ReportMode? = null,
    initialForm: LaporForm = LaporForm(),
    initialCode: String = "",
) {
    var step: LaporStep = initialStep
    var mode: ReportMode? = initialMode
    var form: LaporForm = initialForm
    var code: String = initialCode

    val totalSteps: Int get() = if (mode == ReportMode.Lengkap) 3 else 2

    fun goBack(): Boolean {
        val order = listOf(LaporStep.Choice, LaporStep.Lokasi, LaporStep.Detail, LaporStep.Foto)
        val i = order.indexOf(step)
        if (i <= 0) return false
        step = order[i - 1]
        return true
    }
}
