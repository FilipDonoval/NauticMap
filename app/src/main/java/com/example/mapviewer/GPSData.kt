package com.example.mapviewer

import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.checkSelfPermission

class GPSData(val context: Context, val activity: MainActivity) {
    var locationManager : LocationManager? = null
    var locationListener: LocationListener? = null
    val LOCATION_PERMISSION_REQUEST = 1001  // number doesn't mean anything just for easy comparison

    fun startUp() {
        setupLocation()
        requestLocationPermission()
    }

    fun setupLocation(){
        locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager?
        locationListener = LocationListener { location ->
            Log.d("TAG", "Current Location -  ${location.latitude}:${location.longitude}")
            //locationManager!!.removeUpdates(locationListener)  // for stoping updates
            }
    }

    fun getCurrentLocation(){
        try {
            // Request location updates
            locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 0f, locationListener as LocationListener)
            Log.d("TAG", "Location Request Successful")
        } catch(ex: SecurityException) {
            Log.d("TAG", "Security Exception, no location available")
        }
    }

    fun requestLocationPermission() {
        if (checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(activity, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1001)
        } else {
            // Permission already granted
            getCurrentLocation()
        }
    }
}


