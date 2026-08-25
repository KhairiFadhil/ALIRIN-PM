package com.example.alirinmobile

import android.app.Application
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.preference.PreferenceManager
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.alirinmobile.data.local.AlirinDatabase
import com.example.alirinmobile.data.local.AuthDataStore
import com.example.alirinmobile.data.network.AlirinSupabase
import com.example.alirinmobile.data.network.ApiClient
import com.example.alirinmobile.data.network.PhotoUploader
import com.example.alirinmobile.data.repository.AuthRepository
import com.example.alirinmobile.data.repository.KelurahanRepository
import com.example.alirinmobile.data.repository.LocationRepository
import com.example.alirinmobile.data.repository.PredictionRepository
import com.example.alirinmobile.data.repository.ReportRepository
import com.example.alirinmobile.data.repository.WeatherRepository
import com.example.alirinmobile.data.sync.SyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import java.util.concurrent.TimeUnit

class AlirinApplication : Application() {

    lateinit var apiClient: ApiClient
        private set
    lateinit var authRepository: AuthRepository
        private set
    lateinit var reportRepository: ReportRepository
        private set
    lateinit var weatherRepository: WeatherRepository
        private set
    lateinit var predictionRepository: PredictionRepository
        private set
    lateinit var kelurahanRepository: KelurahanRepository
        private set
    lateinit var locationRepository: LocationRepository
        private set

    val applicationScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        instance = this

        val authStore = AuthDataStore(this)
        val supabase = AlirinSupabase.client
        val uploader = PhotoUploader(supabase)

        apiClient = ApiClient()
        authRepository = AuthRepository(supabase, authStore)
        kelurahanRepository = KelurahanRepository(this)
        val db = AlirinDatabase.get(this)
        weatherRepository = WeatherRepository(apiClient)
        reportRepository = ReportRepository(
            dao = db.reportDao(),
            supabase = supabase,
            uploader = uploader,
            applicationScope = applicationScope,
            authRepo = authRepository,
            kelurahanRepo = kelurahanRepository,
            weatherRepo = weatherRepository,
        )
        predictionRepository = PredictionRepository(apiClient, weatherRepository)
        locationRepository = LocationRepository(this)

        weatherRepository.setSelected(kelurahanRepository.default)

        // First sync + outbox drain di start (best-effort).
        applicationScope.launch {
            runCatching { reportRepository.syncNow() }
            runCatching { reportRepository.retryPending() }
        }

        // Setiap kali network kembali online, drain outbox lagi.
        registerNetworkCallback()

        // Background periodic sync via WorkManager (bekerja walau app di-background).
        val syncWork = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "alirin-sync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncWork,
        )

        @Suppress("DEPRECATION")
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        Configuration.getInstance().load(this, prefs)
        // OSM tile policy: butuh User-Agent jelas + header Referer, kalau tidak diblokir 403.
        Configuration.getInstance().userAgentValue = "ALIRIN-Mobile/1.0 ($packageName)"
        Configuration.getInstance().additionalHttpRequestProperties["Referer"] = "https://alirin.app"
    }

    private fun registerNetworkCallback() {
        val cm = getSystemService(ConnectivityManager::class.java) ?: return
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        val cb = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                applicationScope.launch {
                    runCatching { reportRepository.retryPending() }
                    runCatching { reportRepository.syncNow() }
                }
            }
        }
        runCatching { cm.registerNetworkCallback(request, cb) }
    }

    companion object {
        @Volatile private var instance: AlirinApplication? = null
        fun get(): AlirinApplication = instance ?: error("AlirinApplication not initialized")
    }
}
