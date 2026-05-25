package com.example.alirinmobile.data

import com.example.alirinmobile.data.api.BmkgForecastResponse
import com.example.alirinmobile.data.api.NetworkModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.alirinmobile.data.Kelurahan

/**
 * Pulls hourly forecasts from BMKG's official public API. The adm4 code is a
 * 4-level admin code identifying a specific kelurahan. Default below covers a
 * kelurahan in Bandar Lampung — replace with the right code for the user's area.
 *
 * Why this matters: the raw forecast data (temperature, precipitation, weather code)
 * feeds the future risk-prediction ML pipeline.
 */
class WeatherRepository {
    private val _selected = MutableStateFlow<Kelurahan?>(null)
    /** Currently selected kelurahan; null until KelurahanRepository injects default. */
    val selected: StateFlow<Kelurahan?> = _selected.asStateFlow()

    private val _state = MutableStateFlow<WeatherState>(WeatherState.Idle)
    val state: StateFlow<WeatherState> = _state.asStateFlow()

    fun setSelected(k: Kelurahan) {
        if (_selected.value?.adm4 == k.adm4) return
        _selected.value = k
        _state.value = WeatherState.Idle
    }

    suspend fun refresh(force: Boolean = false) {
        val k = _selected.value ?: return
        if (!force && _state.value is WeatherState.Loaded) return
        _state.value = WeatherState.Loading
        runCatching { NetworkModule.bmkgApi.forecast(k.adm4) }
            .onSuccess { _state.value = WeatherState.Loaded(it) }
            .onFailure { _state.value = WeatherState.Error(it.message ?: "Gagal memuat BMKG") }
    }
}

sealed interface WeatherState {
    data object Idle : WeatherState
    data object Loading : WeatherState
    data class Loaded(val data: BmkgForecastResponse) : WeatherState
    data class Error(val message: String) : WeatherState
}
