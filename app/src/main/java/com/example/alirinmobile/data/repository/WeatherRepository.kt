package com.example.alirinmobile.data.repository

import com.example.alirinmobile.data.network.ApiClient
import com.example.alirinmobile.data.network.dto.BmkgForecastResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface WeatherState {
    data object Idle : WeatherState
    data object Loading : WeatherState
    data class Loaded(val data: BmkgForecastResponse) : WeatherState
    data class Error(val message: String) : WeatherState
}

class WeatherRepository(private val api: ApiClient) {

    private val _selected = MutableStateFlow<Kelurahan?>(null)
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
        runCatching { api.bmkgService.forecast(k.adm4) }
            .onSuccess { _state.value = WeatherState.Loaded(it) }
            .onFailure { _state.value = WeatherState.Error(it.message ?: "Gagal memuat BMKG") }
    }
}
