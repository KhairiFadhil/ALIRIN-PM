package com.example.alirinmobile.data.repository

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class UserLocation(val lat: Double, val lng: Double, val accuracyMeters: Float)

class LocationRepository(private val appContext: Context) {
    private val client = LocationServices.getFusedLocationProviderClient(appContext)

    private val _last = MutableStateFlow<UserLocation?>(null)
    val last: StateFlow<UserLocation?> = _last.asStateFlow()

    private val _permissionGranted = MutableStateFlow(hasFinePermission())
    val permissionGranted: StateFlow<Boolean> = _permissionGranted.asStateFlow()

    fun hasFinePermission(): Boolean =
        ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    // Dipanggil setelah dialog izin dijawab, supaya aliran lokasi ikut hidup
    // tanpa menunggu pengguna berpindah layar.
    fun refreshPermissionState() {
        _permissionGranted.value = hasFinePermission()
    }

    @SuppressLint("MissingPermission")
    suspend fun currentLocation(): UserLocation? {
        if (!hasFinePermission()) return null
        return suspendCancellableCoroutine { cont ->
            client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { loc ->
                    val result = loc?.let { UserLocation(it.latitude, it.longitude, it.accuracy) }
                    result?.let { _last.value = it }
                    cont.resume(result)
                }
                .addOnFailureListener { cont.resume(null) }
        }
    }

    // Aliran posisi berkelanjutan. Sebelumnya aplikasi hanya memanggil
    // getCurrentLocation() sekali, sehingga titik pengguna membeku di posisi
    // pertama dan tidak pernah menyusul saat pengguna berpindah.
    //
    // Pembaruan dikirim tiap 5 detik atau setiap perpindahan 10 meter, mana yang
    // lebih dulu tercapai. Aliran berhenti sendiri saat tidak ada yang mengamati,
    // jadi GPS tidak terus menyala saat layar peta ditutup.
    @SuppressLint("MissingPermission")
    fun locationUpdates(): Flow<UserLocation> = callbackFlow {
        if (!hasFinePermission()) {
            close()
            return@callbackFlow
        }

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL_MS)
            .setMinUpdateDistanceMeters(MIN_DISTANCE_METERS)
            .setMinUpdateIntervalMillis(FASTEST_INTERVAL_MS)
            .setWaitForAccurateLocation(false)
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val loc = result.lastLocation ?: return
                val mapped = UserLocation(loc.latitude, loc.longitude, loc.accuracy)
                _last.value = mapped
                trySend(mapped)
            }
        }

        client.requestLocationUpdates(request, callback, Looper.getMainLooper())
        awaitClose { client.removeLocationUpdates(callback) }
    }

    private companion object {
        const val UPDATE_INTERVAL_MS = 5_000L
        const val FASTEST_INTERVAL_MS = 2_000L
        const val MIN_DISTANCE_METERS = 10f
    }
}
