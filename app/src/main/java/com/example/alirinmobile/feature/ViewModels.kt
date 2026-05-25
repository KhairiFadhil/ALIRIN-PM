package com.example.alirinmobile.feature

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.alirinmobile.AlirinApplication
import com.example.alirinmobile.data.Report
import com.example.alirinmobile.data.ReportMode
import com.example.alirinmobile.data.ReportRepository
import com.example.alirinmobile.data.ReportStatus
import com.example.alirinmobile.data.WeatherRepository
import com.example.alirinmobile.data.WeatherState
import com.example.alirinmobile.data.auth.AuthRepository
import com.example.alirinmobile.data.auth.AuthSession
import com.example.alirinmobile.feature.lapor.LaporForm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private fun repo(): ReportRepository = AlirinApplication.get().repository
private fun authRepo(): AuthRepository = AlirinApplication.get().authRepository
private fun weatherRepo(): WeatherRepository = AlirinApplication.get().weatherRepository
private fun predictionRepo(): com.example.alirinmobile.data.ml.PredictionRepository =
    AlirinApplication.get().predictionRepository
private fun locationRepo(): com.example.alirinmobile.data.LocationRepository =
    AlirinApplication.get().locationRepository

// ── Auth ────────────────────────────────────────────────────────
sealed interface AuthUiState {
    data object Initial : AuthUiState
    data object Submitting : AuthUiState
    data class Failed(val message: String) : AuthUiState
    data class Ok(val session: AuthSession) : AuthUiState
}

class AuthViewModel(private val repository: AuthRepository = authRepo()) : ViewModel() {
    val session: StateFlow<AuthSession?> = repository.session
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val anonChosen: StateFlow<Boolean> = repository.anonChosen
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
    val onboardingDone: StateFlow<Boolean> = repository.onboardingDone
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun finishOnboarding() {
        viewModelScope.launch { repository.markOnboardingDone() }
    }

    val ui = MutableStateFlow<AuthUiState>(AuthUiState.Initial)

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            ui.value = AuthUiState.Failed("Isi username dan password.")
            return
        }
        ui.value = AuthUiState.Submitting
        viewModelScope.launch {
            runCatching { repository.login(username, password) }
                .onSuccess { ui.value = AuthUiState.Ok(it) }
                .onFailure { ui.value = AuthUiState.Failed(it.message ?: "Login gagal.") }
        }
    }

    fun chooseAnonymous() {
        viewModelScope.launch { repository.chooseAnonymous() }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            ui.value = AuthUiState.Initial
        }
    }
}

// ── Reports (citizen-facing) ────────────────────────────────────
class ReportsViewModel(private val repository: ReportRepository = repo()) : ViewModel() {
    val reports: StateFlow<List<Report>> = repository.observeReports()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun report(id: String): StateFlow<Report?> =
        repository.observeReport(id)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}

// ── Lapor flow ──────────────────────────────────────────────────
class LaporViewModel(private val repository: ReportRepository = repo()) : ViewModel() {
    val form = MutableStateFlow(LaporForm())
    val mode = MutableStateFlow<ReportMode?>(null)
    val submitted = MutableStateFlow<String?>(null)

    fun update(newForm: LaporForm) {
        form.value = newForm
        viewModelScope.launch { repository.saveDraft(newForm, mode.value) }
    }

    fun pickMode(m: ReportMode) {
        mode.value = m
        viewModelScope.launch { repository.saveDraft(form.value, m) }
    }

    fun submit() {
        val m = mode.value ?: ReportMode.Cepat
        viewModelScope.launch {
            val result = repository.submitReport(form.value, m)
            submitted.value = result.code
        }
    }

    fun reset() {
        form.value = LaporForm()
        mode.value = null
        submitted.value = null
        viewModelScope.launch { repository.clearDraft() }
    }
}

// ── Staff (approval flows) ──────────────────────────────────────
class StaffViewModel(
    private val repository: ReportRepository = repo(),
    private val authRepository: AuthRepository = authRepo(),
) : ViewModel() {
    /** All reports that still need staff attention. */
    val queue: StateFlow<List<Report>> = repository.observeReports()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val actorLabel: StateFlow<String?> = authRepository.session
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
        .let { sessFlow ->
            MutableStateFlow<String?>(null).also { out ->
                viewModelScope.launch {
                    sessFlow.collect { out.value = it?.displayName }
                }
            }
        }

    fun transition(reportId: String, newStatus: ReportStatus, note: String?) {
        viewModelScope.launch {
            repository.updateReportStatus(
                reportId = reportId,
                newStatus = newStatus,
                note = note,
                actorLabel = actorLabel.value,
            )
        }
    }
}

// ── Weather (BMKG) — exposed for future ML pipeline ─────────────
class WeatherViewModel(
    private val repository: WeatherRepository = weatherRepo(),
    private val kelurahanRepo: com.example.alirinmobile.data.KelurahanRepository =
        AlirinApplication.get().kelurahanRepository,
) : ViewModel() {
    val state: StateFlow<WeatherState> = repository.state
    val selected: StateFlow<com.example.alirinmobile.data.Kelurahan?> = repository.selected
    val list: List<com.example.alirinmobile.data.Kelurahan> get() = kelurahanRepo.list

    init {
        // Fire an initial fetch when the VM comes alive (no-op if already loaded).
        viewModelScope.launch { repository.refresh() }
    }

    fun pick(k: com.example.alirinmobile.data.Kelurahan) {
        repository.setSelected(k)
        viewModelScope.launch { repository.refresh(force = true) }
    }

    fun refresh() {
        viewModelScope.launch { repository.refresh(force = true) }
    }
}

// ── Prediction (rule-based ML) ──────────────────────────────────
class PredictionViewModel(
    private val repository: com.example.alirinmobile.data.ml.PredictionRepository = predictionRepo(),
) : ViewModel() {
    val prediction: StateFlow<com.example.alirinmobile.data.ml.PredictionResult?> =
        repository.observe().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            null,
        )
}

// ── Location ────────────────────────────────────────────────────
class LocationViewModel(
    private val repository: com.example.alirinmobile.data.LocationRepository = locationRepo(),
) : ViewModel() {
    val last: StateFlow<com.example.alirinmobile.data.UserLocation?> = repository.last

    fun hasPermission(): Boolean = repository.hasFinePermission()

    fun fetchOnce(onResult: (com.example.alirinmobile.data.UserLocation?) -> Unit = {}) {
        viewModelScope.launch { onResult(repository.currentLocation()) }
    }
}

object AlirinViewModelFactory {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer { AuthViewModel() }
        initializer { ReportsViewModel() }
        initializer { LaporViewModel() }
        initializer { StaffViewModel() }
        initializer { WeatherViewModel() }
        initializer { PredictionViewModel() }
        initializer { LocationViewModel() }
    }
}
