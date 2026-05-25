package com.example.alirinmobile.feature.lapor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.alirinmobile.data.ReportMode
import com.example.alirinmobile.feature.LaporViewModel

@Composable
fun LaporFlowScreen(
    viewModel: LaporViewModel,
    onClose: () -> Unit,
    onGoToStatus: () -> Unit,
) {
    var step by remember { mutableStateOf(LaporStep.Choice) }
    val form by viewModel.form.collectAsStateWithLifecycle()
    val mode by viewModel.mode.collectAsStateWithLifecycle()
    val submitted by viewModel.submitted.collectAsStateWithLifecycle()

    val totalSteps = if (mode == ReportMode.Lengkap) 3 else 2

    LaunchedEffect(submitted) {
        if (submitted != null && step != LaporStep.Success) step = LaporStep.Success
    }

    fun back() {
        val order = listOf(LaporStep.Choice, LaporStep.Lokasi, LaporStep.Detail, LaporStep.Foto)
        val i = order.indexOf(step)
        if (i <= 0) onClose() else step = order[i - 1]
    }

    when (step) {
        LaporStep.Choice -> ChoiceStep(
            onPick = { picked ->
                viewModel.pickMode(picked)
                step = LaporStep.Lokasi
            },
            onBack = onClose,
        )
        LaporStep.Lokasi -> LokasiStep(
            form = form,
            onUpdate = { viewModel.update(it) },
            onNext = { step = LaporStep.Detail },
            onBack = { back() },
            step = 1, total = totalSteps,
        )
        LaporStep.Detail -> DetailStep(
            form = form,
            onUpdate = { viewModel.update(it) },
            mode = mode ?: ReportMode.Cepat,
            onNext = {
                if (mode == ReportMode.Lengkap) step = LaporStep.Foto else viewModel.submit()
            },
            onBack = { back() },
            step = 2, total = totalSteps,
        )
        LaporStep.Foto -> FotoStep(
            form = form,
            onUpdate = { viewModel.update(it) },
            onNext = { viewModel.submit() },
            onBack = { back() },
            step = 3, total = 3,
        )
        LaporStep.Success -> SuccessScreen(
            code = submitted.orEmpty(),
            mode = mode ?: ReportMode.Cepat,
            form = form,
            onClose = onClose,
            onGoToStatus = onGoToStatus,
        )
    }
}
