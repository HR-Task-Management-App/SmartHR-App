package com.example.smarthr_app.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LocationHelper(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    suspend fun getCurrentLocation(): Location? = suspendCancellableCoroutine { continuation ->
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                continuation.resume(location)
            }
            .addOnFailureListener {
                continuation.resume(null)
            }
    }

    fun isWithinRadius(
        currentLat: String,
        currentLng: String,
        officeLat: String,
        officeLng: String,
        radiusInMeters: String
    ): Boolean {
        return try {
            val results = FloatArray(1)
            Location.distanceBetween(
                currentLat.toDouble(), currentLng.toDouble(),
                officeLat.toDouble(), officeLng.toDouble(),
                results
            )
            results[0] <= radiusInMeters.toDouble()
        } catch (e: Exception) {
            false
        }
    }

    fun getDistanceInMeters(
        lat1: String,
        lng1: String,
        lat2: String,
        lng2: String
    ): Float {
        return try {
            val results = FloatArray(1)
            Location.distanceBetween(
                lat1.toDouble(), lng1.toDouble(),
                lat2.toDouble(), lng2.toDouble(),
                results
            )
            results[0]
        } catch (e: Exception) {
            Float.MAX_VALUE
        }
    }
}