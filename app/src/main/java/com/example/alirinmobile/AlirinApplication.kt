package com.example.alirinmobile

import android.app.Application
import android.preference.PreferenceManager
import com.example.alirinmobile.data.KelurahanRepository
import com.example.alirinmobile.data.LocationRepository
import com.example.alirinmobile.data.ReportRepository
import com.example.alirinmobile.data.WeatherRepository
import com.example.alirinmobile.data.ml.PredictionRepository
import com.example.alirinmobile.data.auth.AuthRepository
import com.example.alirinmobile.data.local.AuthDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration

class AlirinApplication : Application() {

    lateinit var repository: ReportRepository
        private set
    lateinit var authRepository: AuthRepository
        private set
    lateinit var weatherRepository: WeatherRepository
        private set
    lateinit var kelurahanRepository: KelurahanRepository
        private set
    lateinit var predictionRepository: PredictionRepository
        private set
    lateinit var locationRepository: LocationRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        repository = ReportRepository(this)
        authRepository = AuthRepository(AuthDataStore(this))
        weatherRepository = WeatherRepository()
        kelurahanRepository = KelurahanRepository(this)
        predictionRepository = PredictionRepository(weatherRepository, repository)
        locationRepository = LocationRepository(this)
        // Network must know about the auth store so its Authenticator can refresh tokens.
        com.example.alirinmobile.data.api.NetworkModule.init(AuthDataStore(this))
        // Seed default selection so WeatherViewModel can refresh on first read.
        weatherRepository.setSelected(kelurahanRepository.default)

        // osmdroid config (user agent required by tile provider TOS)
        @Suppress("DEPRECATION")
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        Configuration.getInstance().load(this, prefs)
        Configuration.getInstance().userAgentValue = packageName

        appScope.launch { repository.seedIfEmpty() }
    }

    companion object {
        private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        @Volatile private var instance: AlirinApplication? = null
        fun get(): AlirinApplication = instance ?: error("AlirinApplication not initialized")
    }
}
