package com.example.alirinmobile.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class UserLocation(val lat: Double, val lng: Double, val accuracyMeters: Float)

/**
 * Wraps FusedLocationProviderClient. Permission checks stay here so callers don't have to
 * touch ContextCompat directly.
 */
class LocationRepository(private val appContext: Context) {
    private val client = LocationServices.getFusedLocationProviderClient(appContext)

    private val _last = MutableStateFlow<UserLocation?>(null)
    val last: StateFlow<UserLocation?> = _last.asStateFlow()

    fun hasFinePermission(): Boolean =
        ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

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
}
