package com.androidshowtime.uberclone.driver


import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.androidshowtime.uberclone.databinding.FragmentDriverBinding
import com.google.android.gms.location.*
import com.google.firebase.firestore.FirebaseFirestore
import timber.log.Timber

class DriverFragment : Fragment() {

    //vars
    private lateinit var requestsLists: MutableList<String>
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var firestore: FirebaseFirestore


    //request location permission
    val requestLocationPermission = registerForActivityResult(
        ActivityResultContracts
            .RequestPermission()
                                                             ) {


        if (it) {

            startLocationUpdates()
        } else {

            Toast.makeText(activity, "Location Permission needed", Toast.LENGTH_SHORT).show()
        }
    }


    //location updates components
    private lateinit var currentLocation: Location
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private var locationCallback: LocationCallback = object : LocationCallback() {

        override fun onLocationResult(locationResult: LocationResult?) {
            super.onLocationResult(locationResult)

            if (locationResult != null) {

                locationResult.locations.forEach{currentLocation = it}
                Timber.i("Driver's Location is: ${currentLocation.latitude}, ${currentLocation.longitude}")
            } else {
                //Log Driver's location as null
                Timber.i("Current location is null")
                return
            }
        }


    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
                             ): View? {
        //initialize firestore
        firestore = FirebaseFirestore.getInstance()
        //request permission
        requestLocationPermission.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)

        //initialize fusedLocationProviderClient
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

        //initialize locationRequest
        locationRequest = LocationRequest().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 5000
            fastestInterval = 3000
        }


        val binding = FragmentDriverBinding.inflate(inflater)

        (activity as AppCompatActivity).supportActionBar?.show()
        (activity as AppCompatActivity).supportActionBar?.title = "Nearby Requests"


       requestsLists = mutableListOf()
        requestsLists.add("Test")

 adapter = ArrayAdapter<String>(requireActivity(), android.R.layout
                .simple_list_item_1, requestsLists
                                          )

        binding.listView.adapter = adapter




       //val docRef = firestore.collection("Locations").document("DriverLocation")


        return binding.root
    }

    //startLocationUpdates() to be called once permission has been granted
    fun startLocationUpdates() {

        try {
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
                                                              )
        } catch (e: SecurityException) {
            //Create a function to request necessary permissions from the app.

        }
    }


    override fun onStop() {
        super.onStop()

        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

}