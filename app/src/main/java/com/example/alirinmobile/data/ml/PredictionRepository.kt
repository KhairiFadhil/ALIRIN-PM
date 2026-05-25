package com.example.alirinmobile.data.ml

import com.example.alirinmobile.data.ReportRepository
import com.example.alirinmobile.data.WeatherRepository
import com.example.alirinmobile.data.WeatherState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Reactive risk prediction: re-emits whenever the BMKG forecast, the active-reports list,
 * or the selected kelurahan changes.
 */
class PredictionRepository(
    private val weather: WeatherRepository,
    private val reports: ReportRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observe(): Flow<PredictionResult> = combine(
        weather.state,
        weather.selected,
        reports.observeReports(),
    ) { weatherState, kelurahan, reportsList ->
        val forecast = (weatherState as? WeatherState.Loaded)?.data
        RiskPredictor.predict(forecast = forecast, reports = reportsList, kelurahan = kelurahan)
    }
}
