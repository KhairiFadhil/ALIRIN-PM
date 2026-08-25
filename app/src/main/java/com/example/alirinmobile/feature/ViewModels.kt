package com.example.alirinmobile.feature

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.alirinmobile.AlirinApplication
import com.example.alirinmobile.data.Report
import com.example.alirinmobile.data.ReportMode
import com.example.alirinmobile.data.ReportStatus
import com.example.alirinmobile.data.auth.AuthSession
import com.example.alirinmobile.data.repository.AuthRepository
import com.example.alirinmobile.data.repository.Kelurahan
import com.example.alirinmobile.data.repository.KelurahanRepository
import com.example.alirinmobile.data.repository.LocationRepository
import com.example.alirinmobile.data.repository.PredictionRepository
import com.example.alirinmobile.data.repository.PredictionUiModel
import com.example.alirinmobile.data.repository.ReportRepository
import com.example.alirinmobile.data.repository.UserLocation
import com.example.alirinmobile.data.repository.WeatherRepository
import com.example.alirinmobile.data.repository.WeatherState
import com.example.alirinmobile.feature.lapor.LaporForm
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private fun repo(): ReportRepository = AlirinApplication.get().reportRepository
private fun authRepo(): AuthRepository = AlirinApplication.get().authRepository
private fun weatherRepo(): WeatherRepository = AlirinApplication.get().weatherRepository
private fun predictionRepo(): PredictionRepository = AlirinApplication.get().predictionRepository
private fun locationRepo(): LocationRepository = AlirinApplication.get().locationRepository
private fun kelurahanRepo(): KelurahanRepository = AlirinApplication.get().kelurahanRepository

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

    val ui = MutableStateFlow<AuthUiState>(AuthUiState.Initial)

    fun finishOnboarding() {
        viewModelScope.launch { repository.markOnboardingDone() }
    }

    // Nama parameter tetap "username" untuk backward-compat dengan LoginScreen,
    // tapi sekarang Supabase menerima email address. Passthrough saja.
    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            ui.value = AuthUiState.Failed("Isi email dan password.")
            return
        }
        ui.value = AuthUiState.Submitting
        viewModelScope.launch {
            runCatching { repository.login(username, password) }
                .onSuccess { ui.value = AuthUiState.Ok(it) }
                .onFailure { ui.value = AuthUiState.Failed(it.message ?: "Login gagal. Coba lagi.") }
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

class ReportsViewModel(private val repository: ReportRepository = repo()) : ViewModel() {
    val reports: StateFlow<List<Report>> = repository.observeReports()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Laporan yang dibuat dari perangkat ini, plus yang pernah dilacak lewat
    // token. Layar Status dulu menampilkan seluruh laporan kota dengan judul
    // "Status Laporan" dan blok bernama MyReports.
    val myReports: StateFlow<List<Report>> = repository.observeMyReports()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        // Fire a sync when this VM is first observed. Best-effort; failures are logged only.
        viewModelScope.launch { runCatching { repository.syncNow() } }
    }

    fun report(id: String): StateFlow<Report?> =
        repository.observeReport(id)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun refresh() {
        viewModelScope.launch { runCatching { repository.syncNow() } }
    }

    suspend fun trackByToken(token: String): Report? = repository.trackByToken(token)
}

class LaporViewModel(private val repository: ReportRepository = repo()) : ViewModel() {
    val form = MutableStateFlow(LaporForm())
    val mode = MutableStateFlow<ReportMode?>(null)

    // "submitted" masih memegang code laporan (untuk backward-compat dengan
    // LaporFlowScreen yang trigger navigate ke SuccessScreen bila non-null).
    val submitted = MutableStateFlow<String?>(null)
    val submittedToken = MutableStateFlow<String?>(null)
    val submittedId = MutableStateFlow<String?>(null)
    val submitError = MutableStateFlow<String?>(null)
    val submitting = MutableStateFlow(false)

    fun update(newForm: LaporForm) {
        form.value = newForm
        repository.saveDraft(newForm)
    }

    fun pickMode(m: ReportMode) {
        mode.value = m
        repository.saveDraft(form.value)
    }

    fun submit() {
        if (submitting.value) return
        val m = mode.value ?: ReportMode.Cepat
        submitting.value = true
        submitError.value = null
        viewModelScope.launch {
            val result = repository.submit(form.value, m)
            submitting.value = false
            result
                .onSuccess {
                    submitted.value = it.code
                    submittedToken.value = it.trackingToken
                    submittedId.value = it.id
                }
                .onFailure {
                    submitError.value = it.message ?: "Gagal menyimpan laporan. Coba lagi."
                }
        }
    }

    fun reset() {
        form.value = LaporForm()
        mode.value = null
        submitted.value = null
        submittedToken.value = null
        submittedId.value = null
        submitError.value = null
        submitting.value = false
        repository.clearDraft()
    }
}

class StaffViewModel(
    private val repository: ReportRepository = repo(),
    private val authRepository: AuthRepository = authRepo(),
) : ViewModel() {

    private val _officerId = MutableStateFlow<String?>(null)

    // Inbox verifikasi: hanya laporan yang belum ditugaskan atau ditugaskan ke
    // petugas ini. Sebelumnya seluruh laporan kota tampil, sehingga petugas mana
    // pun bisa menutup laporan siapa pun (Proposal 6.4.2 mengandaikan inbox
    // tugas sendiri).
    val queue: StateFlow<List<Report>> = combine(
        repository.observeReports(),
        _officerId,
    ) { reports, officerId ->
        reports.filter { it.assignedOfficerId.isNullOrBlank() || it.assignedOfficerId == officerId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Tugas saya: hanya yang benar-benar ditugaskan ke petugas ini.
    val myTasks: StateFlow<List<Report>> = combine(
        repository.observeReports(),
        _officerId,
    ) { reports, officerId ->
        if (officerId.isNullOrBlank()) emptyList()
        else reports.filter { it.assignedOfficerId == officerId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _actor = MutableStateFlow<String?>(null)
    val actorLabel: StateFlow<String?> = _actor

    val error = MutableStateFlow<String?>(null)
    fun clearError() { error.value = null }

    init {
        viewModelScope.launch {
            authRepository.session.collect {
                _actor.value = it?.displayName
                _officerId.value = it?.officerId
            }
        }
        viewModelScope.launch { runCatching { repository.syncNow() } }
    }

    fun transition(reportId: String, newStatus: ReportStatus, note: String?) {
        repository.updateReportStatus(
            reportId = reportId,
            newStatus = newStatus,
            note = note,
            actorLabel = _actor.value,
            onError = { error.value = it },
        )
    }

    fun verifyWithPhoto(reportId: String, photoLocalPath: String, note: String?) {
        repository.verifyWithPhoto(reportId, photoLocalPath, note, _actor.value)
    }
}

class WeatherViewModel(
    private val repository: WeatherRepository = weatherRepo(),
    private val kelurahanRepo: KelurahanRepository = kelurahanRepo(),
) : ViewModel() {
    val state: StateFlow<WeatherState> = repository.state
    val selected: StateFlow<Kelurahan?> = repository.selected
    val list: List<Kelurahan> get() = kelurahanRepo.list

    init {
        viewModelScope.launch { repository.refresh() }
    }

    fun pick(k: Kelurahan) {
        repository.setSelected(k)
        viewModelScope.launch { repository.refresh(force = true) }
    }

    fun refresh() {
        viewModelScope.launch { repository.refresh(force = true) }
    }
}

class PredictionViewModel(
    private val repository: PredictionRepository = predictionRepo(),
) : ViewModel() {
    val prediction: StateFlow<PredictionUiModel?> =
        repository.observe().stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            null,
        )
}

class LocationViewModel(
    private val repository: LocationRepository = locationRepo(),
) : ViewModel() {
    val last: StateFlow<UserLocation?> = repository.last

    fun hasPermission(): Boolean = repository.hasFinePermission()

    fun fetchOnce(onResult: (UserLocation?) -> Unit = {}) {
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
