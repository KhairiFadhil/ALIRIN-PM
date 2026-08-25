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

    // Curah hujan untuk faktor Cuaca pada risk score (Proposal 4.4).
    // BMKG mengembalikan slot berdurasi 3 jam, jadi "prakiraan 3 jam ke depan"
    // berarti SATU slot pertama, bukan tiga slot (tiga slot = 9 jam).
    // null berarti BMKG tidak terjangkau: bobot cuaca dialihkan, bukan dinolkan.
    suspend fun rainfallMmFor(adm4: String): Double? = runCatching {
        val forecast = api.bmkgService.forecast(adm4)
        forecast.data.firstOrNull()?.cuaca?.flatten()?.firstOrNull()?.tp ?: 0.0
    }.getOrNull()
}
