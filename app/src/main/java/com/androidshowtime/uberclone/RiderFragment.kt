package com.androidshowtime.uberclone

import android.annotation.SuppressLint
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
private lateinit var lastKnownLocation:LatLng

    //components for locations request
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
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


        //creating LocationRequest
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000)
                .setFastestInterval(3000)


        //initialize locationCallback to get notification
        locationCallback = object : LocationCallback() {

            override fun onLocationResult(p0: LocationResult?) {
                super.onLocationResult(p0)
            }

            override fun onLocationAvailability(p0: LocationAvailability?) {
                super.onLocationAvailability(p0)
            }

        }








        return inflater.inflate(R.layout.fragment_rider, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {


    }
    // Single Permission Contract

    @SuppressLint("MissingPermission") val reqPerm =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { resut ->

                if (resut) {

                    Timber.i("Location Permission Granted")

                    fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                                                                       locationCallback,
                                                                       Looper.getMainLooper())
                }
                else {


                    Timber.i("Location Permission Denied")
                }


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

}