package com.example.photoweather.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.example.photoweather.R
import com.example.photoweather.utils.AppUtils
import com.example.photoweather.viewModel.BaseViewModel
import com.google.android.gms.location.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: BaseViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProvider(this)[BaseViewModel::class.java]
        initLocation()
        getData()
    }

    private fun getData() {
        if (AppUtils.checkPermissions(this, 1)) {
            viewModel.getAllImages()
        }
    }

    private fun initLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationRequest()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations){
                    Log.i(TAG, "onLocationResult: last location = $location")
                    viewModel.lastLocation.value = location
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            0 -> {
                if (grantResults.isNotEmpty() && AppUtils.allPermissionsGranted(grantResults)) {
                    viewModel.getAllImages()
                } else {
                    AppUtils.makeToast(
                        this,
                        resources.getString(R.string.access_camera_permission_denied)
                    )
                }
                return
            }
            1 -> {
                if (grantResults.isNotEmpty() && AppUtils.allPermissionsGranted(grantResults)) {
                    viewModel.getAllImages()
                } else {
                    AppUtils.makeToast(
                        this,
                        resources.getString(R.string.access_camera_permission_denied)
                    )
                }
            }
        }
    }

    override fun onBackPressed() {
        val currentDestination = this.findNavController(R.id.nav_host_fragment).currentDestination
        if (currentDestination?.id == R.id.create_image_fragment) {
            viewModel.deleteImage()
        }
        super.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressed()
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        if (AppUtils.checkLocationPermissions(this, 2)) startLocationUpdates()
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            AppUtils.getLocationPermissions(this, 3)
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.interval = UPDATE_INTERVAL_IN_MIL
        locationRequest.fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MIL
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val UPDATE_INTERVAL_IN_MIL: Long = 30000
        private const val FASTEST_UPDATE_INTERVAL_IN_MIL = UPDATE_INTERVAL_IN_MIL / 2
    }
}