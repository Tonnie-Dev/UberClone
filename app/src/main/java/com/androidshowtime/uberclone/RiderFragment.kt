package com.androidshowtime.uberclone

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import timber.log.Timber

class RiderFragment : Fragment() {
    private lateinit var lastKnownLocation: LatLng
    private lateinit var currentLocation: Location

    // Single Permission Contract
    @SuppressLint("MissingPermission")
    val reqPerm = registerForActivityResult(ActivityResultContracts.RequestPermission()) {

        if (it) {
            Timber.i("Location Permission Granted")

            //request for Location Updates
            startLocationUpdates()
        }
        else {
            Toast.makeText(activity, "Location Permission Needed",
                           Toast.LENGTH_SHORT).show()
        }

<<<<<<< HEAD
        Timber.i("Location Permission Granted - Rider")
||||||| ab492bb
        Timber.i("Location Permission Granted")
=======
>>>>>>> locationupdates
    }


<<<<<<< HEAD
        Timber.i("Location Permission Denied - Rider")
||||||| ab492bb
        Timber.i("Location Permission Denied")
=======
    //components for locations request
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    //initialize locationCallback to get notification
    private val locationCallback = object : LocationCallback() {

        //Called when the device location is available.
        override fun onLocationResult(locationResult: LocationResult?) {
            super.onLocationResult(locationResult)
            Timber.i("onLocationResult Triggered")

            if (locationResult != null) {
                //locationResult.locations.forEach { currentLocation = it }
                currentLocation = locationResult.lastLocation
                Timber.i(
                    "Current Place:  ${currentLocation.latitude}, ${currentLocation.longitude}")
            }
            else {

                Timber.i("Location is null!!!")
                return

            }
        }


>>>>>>> locationupdates
    }


    private val callback = OnMapReadyCallback { googleMap ->
        val sydney = LatLng(-34.0, 151.0)
        googleMap.addMarker(MarkerOptions().position(sydney)
                                    .title("Marker in Sydney"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
                             ): View? {

        //request Permission
        reqPerm.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
        //initializing the fusedLocationProviderClient
        fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(requireActivity())
        locationRequest = LocationRequest().apply {


            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 3000
            fastestInterval = 5000
        }


        return inflater.inflate(R.layout.fragment_rider, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

    }


    //request permission
    @SuppressLint("MissingPermission")
    fun getLastKnownLocation(): LatLng {

        //lastLocation returns a task object
        fusedLocationProviderClient.lastLocation.apply {
            addOnSuccessListener {

                //the lastLocation may be null sometimes
                it?.let {

                    //retrieving values from Location
                    val myLat = it.latitude
                    val myLng = it.longitude
                    lastKnownLocation = LatLng(myLat, myLng)
                }
            }

            addOnFailureListener {
                //You can show an error dialogue or a toast stating the failure
            }

        }
        return lastKnownLocation

    }


    //start Location Updates
    private fun startLocationUpdates() {
        try {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest, locationCallback,
                Looper.getMainLooper())
        }
        catch (e: SecurityException) {
            //Create a function to request necessary permissions from the app.

        }
    }

    override fun onStop() {
        super.onStop()

        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

}