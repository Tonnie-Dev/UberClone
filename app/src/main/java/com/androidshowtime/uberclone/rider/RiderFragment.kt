package com.androidshowtime.uberclone.rider

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
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.androidshowtime.uberclone.MyViewModel
import com.androidshowtime.uberclone.R
import com.androidshowtime.uberclone.databinding.FragmentRiderBinding
import com.androidshowtime.uberclone.model.User
import com.androidshowtime.uberclone.model.UserLocation
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import timber.log.Timber
import java.util.*

class RiderFragment : Fragment() {
    //vars
    private lateinit var map: GoogleMap
    private lateinit var currentLocation: Location
    private lateinit var firestoreDB: FirebaseFirestore
    private lateinit var userType: String
    private val args: RiderFragmentArgs by navArgs()

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
                           Toast.LENGTH_SHORT)
                    .show()
        }


    }


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

                //positioning the camera and the marker
                moveMarkerAndCamera(currentLocation)






            }
            else {

                Timber.i("Location is null!!!")
                return

            }
        }


    }

    /*onMapReadyCallBack - is triggered when the map is ready to be used
  and provides a non-null instance of GoogleMap.*/


    private val onMapReadyCallback = OnMapReadyCallback {
        //initialize map to it
        map = it

        //request Permission
        reqPerm.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        val binding = FragmentRiderBinding.inflate(inflater)
        val viewModel = ViewModelProvider(this).get(MyViewModel::class.java)
        binding.viewModel = viewModel




        //initializing the fusedLocationProviderClient
        fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(requireActivity())

        //initializing locationRequest
        locationRequest = LocationRequest().apply {


            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 5000
          fastestInterval = 3000
        }

        //initializing Firestore
        firestoreDB = FirebaseFirestore.getInstance()


        //Call Uber Button implementation
        binding.callUberButton.setOnClickListener {

            //create Firestore Geopoint variable
            val geoPoint = GeoPoint(currentLocation.latitude, currentLocation.longitude)

            val uid = FirebaseAuth.getInstance().uid!!
            val userType = args.userType
val  userLocation = UserLocation(User(uid, userType), geoPoint, Date())

            //save userLocation on firestore
            firestoreDB.collection("User Location").add(userLocation)
            Toast.makeText(activity, "Button Clicked", Toast.LENGTH_SHORT)
                    .show()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(onMapReadyCallback)

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

    //move camera around
    fun moveMarkerAndCamera(location: Location) {
        //clear map before setting the marker

        map.clear()
        //obtain currentLatLng from the currentLocation
        val currentLatLng = LatLng(location.latitude, location.longitude)
        map.addMarker(MarkerOptions().position(currentLatLng)
                              .title("Your Location"))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 5f))
    }




}


