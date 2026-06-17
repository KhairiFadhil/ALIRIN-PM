package com.example.alirinmobile

import android.app.Application
import android.preference.PreferenceManager
import com.example.alirinmobile.data.local.AlirinDatabase
import com.example.alirinmobile.data.local.AuthDataStore
import com.example.alirinmobile.data.network.ApiClient
import com.example.alirinmobile.data.repository.AuthRepository
import com.example.alirinmobile.data.repository.KelurahanRepository
import com.example.alirinmobile.data.repository.LocationRepository
import com.example.alirinmobile.data.repository.PredictionRepository
import com.example.alirinmobile.data.repository.ReportRepository
import com.example.alirinmobile.data.repository.WeatherRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration

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

    override fun onCreate() {
        super.onCreate()
        instance = this

        val authStore = AuthDataStore(this)
        apiClient = ApiClient(authStore)
        authRepository = AuthRepository(apiClient, authStore)
        val db = AlirinDatabase.get(this)
        reportRepository = ReportRepository(db.reportDao())
        weatherRepository = WeatherRepository(apiClient)
        predictionRepository = PredictionRepository(apiClient, weatherRepository)
        kelurahanRepository = KelurahanRepository(this)
        locationRepository = LocationRepository(this)

        weatherRepository.setSelected(kelurahanRepository.default)

        CoroutineScope(Dispatchers.IO).launch { reportRepository.seedIfEmpty() }

        @Suppress("DEPRECATION")
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        Configuration.getInstance().load(this, prefs)
        Configuration.getInstance().userAgentValue = packageName
    }

    companion object {
        @Volatile private var instance: AlirinApplication? = null
        fun get(): AlirinApplication = instance ?: error("AlirinApplication not initialized")
    }
}
