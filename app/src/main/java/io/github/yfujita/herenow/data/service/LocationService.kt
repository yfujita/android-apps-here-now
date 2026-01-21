package io.github.yfujita.herenow.data.service

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class LocationService(private val client: FusedLocationProviderClient) {
    @SuppressLint("MissingPermission") // Caller must ensure permission
    fun getLocationUpdates(intervalMillis: Long): Flow<Location> =
        callbackFlow {
            val request: LocationRequest =
                LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMillis)
                    .setWaitForAccurateLocation(false)
                    .setMinUpdateIntervalMillis(intervalMillis)
                    .build()

            val callback: LocationCallback =
                object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        result.lastLocation?.let { trySend(it) }
                    }
                }

            // Try to get the last known location immediately
            client.lastLocation.addOnSuccessListener { location ->
                location?.let { trySend(it) }
            }

            client.requestLocationUpdates(request, callback, Looper.getMainLooper())

            awaitClose {
                client.removeLocationUpdates(callback)
            }
        }
}
