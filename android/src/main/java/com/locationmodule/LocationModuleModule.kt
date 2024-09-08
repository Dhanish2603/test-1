package com.locationmodule

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.Arguments

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

    val location: Location? = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

    if (location != null) {
      val result: WritableMap = Arguments.createMap()
      result.putDouble("latitude", location.latitude)
      result.putDouble("longitude", location.longitude)
      promise.resolve(result)
    } else {
      promise.reject("LOCATION_UNAVAILABLE", "Unable to get location")
    }
  }

  companion object {
    const val NAME = "LocationModule"
  }
}