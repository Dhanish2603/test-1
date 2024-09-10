package com.locationmodule

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.Arguments
import java.net.InetAddress

class LocationModuleModule internal constructor(context: ReactApplicationContext) :
  LocationModuleSpec(context) {

  private val reactContext: ReactApplicationContext = context

  override fun getName(): String {
    return NAME
  }

  @ReactMethod
  override fun multiply(a: Double, b: Double, promise: Promise) {
    promise.resolve(a * b)
  }

  @ReactMethod
  override fun locationfind(promise: Promise) {
    val locationManager = reactContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    if (ActivityCompat.checkSelfPermission(
        reactContext,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
        reactContext,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) != PackageManager.PERMISSION_GRANTED
    ) {
        promise.reject("PERMISSION_DENIED", "Location permission not granted")
        return
    }

    val networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    val gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

    when {
        isInternetAvailable() && networkEnabled -> 
            requestLocationUpdates(locationManager, LocationManager.NETWORK_PROVIDER, promise)
        gpsEnabled -> 
            requestLocationUpdates(locationManager, LocationManager.GPS_PROVIDER, promise)
        else -> {
            val lastKnownLocation: Location? = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            if (lastKnownLocation != null) {
                resolveLocation(lastKnownLocation, promise)
            } else {
                promise.reject("LOCATION_UNAVAILABLE", "Unable to get location and no last known location available")
            }
        }
    }
  }

  private fun isInternetAvailable(): Boolean {
    return try {
        val ipAddr: InetAddress = InetAddress.getByName("google.com")
        !ipAddr.equals("")
    } catch (e: Exception) {
        false
    }
  }

  private fun requestLocationUpdates(locationManager: LocationManager, provider: String, promise: Promise) {
    val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            locationManager.removeUpdates(this)
            resolveLocation(location, promise)
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {
            // If the current provider is disabled, try the other one
            val otherProvider = if (provider == LocationManager.NETWORK_PROVIDER) 
                                    LocationManager.GPS_PROVIDER 
                                else 
                                    LocationManager.NETWORK_PROVIDER
            if (locationManager.isProviderEnabled(otherProvider)) {
                locationManager.removeUpdates(this)
                requestLocationUpdates(locationManager, otherProvider, promise)
            }
        }
    }

    try {
        locationManager.requestLocationUpdates(provider, 0L, 0f, locationListener)
    } catch (e: Exception) {
        // If requesting updates fails (e.g., no internet for NETWORK_PROVIDER), 
        // try the other provider
        val otherProvider = if (provider == LocationManager.NETWORK_PROVIDER) 
                                LocationManager.GPS_PROVIDER 
                            else 
                                LocationManager.NETWORK_PROVIDER
        if (locationManager.isProviderEnabled(otherProvider)) {
            requestLocationUpdates(locationManager, otherProvider, promise)
        } else {
            promise.reject("LOCATION_UNAVAILABLE", "Unable to get location update")
        }
        return
    }

    // Set a timeout in case we can't get a location update
    reactContext.runOnUiQueueThread {
        android.os.Handler().postDelayed({
            locationManager.removeUpdates(locationListener)
            val lastKnownLocation = locationManager.getLastKnownLocation(provider)
                ?: locationManager.getLastKnownLocation(if (provider == LocationManager.NETWORK_PROVIDER) 
                                                            LocationManager.GPS_PROVIDER 
                                                        else 
                                                            LocationManager.NETWORK_PROVIDER)
            if (lastKnownLocation != null) {
                resolveLocation(lastKnownLocation, promise)
            } else {
                promise.reject("LOCATION_UNAVAILABLE", "Unable to get location update and no last known location available")
            }
        }, 30000) // 30 second timeout
    }
  }

  private fun resolveLocation(location: Location, promise: Promise) {
    val result: WritableMap = Arguments.createMap()
    result.putDouble("latitude", location.latitude)
    result.putDouble("longitude", location.longitude)
    promise.resolve(result)
  }

  companion object {
    const val NAME = "LocationModule"
  }
}