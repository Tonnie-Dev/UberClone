package com.androidshowtime.uberclone.rider

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.androidshowtime.uberclone.MyViewModel
import com.androidshowtime.uberclone.R
import com.androidshowtime.uberclone.databinding.FragmentRiderBinding
import com.androidshowtime.uberclone.model.UberRequest
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.Source
import timber.log.Timber
import java.util.*

class RiderFragment : Fragment() {
    //vars
    private lateinit var map: GoogleMap
    private lateinit var currentLocation: Location
    private lateinit var firestore: FirebaseFirestore
    private lateinit var currentUserId: String
    private var isButtonClicked = false
    private lateinit var docID: String
    private lateinit var handler: Handler
    private var isRequestAccepted = false
    private lateinit var binding: FragmentRiderBinding


    // Single Permission Contract
    @SuppressLint("MissingPermission")
    val reqPerm = registerForActivityResult(ActivityResultContracts.RequestPermission()) {

        if (it) {
            Timber.i("Location Permission Granted")

            //request for Location Updates
            startLocationUpdates()
        }
        else {
            Toast.makeText(
                    activity, "Location Permission Needed",
                    Toast.LENGTH_SHORT
                          )
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


            if (locationResult != null) {
                //locationResult.locations.forEach { currentLocation = it }
                currentLocation = locationResult.lastLocation
                Timber.i(
                        "Current Place:  ${currentLocation.latitude}, ${currentLocation.longitude}"
                        )

                //positioning the camera and the marker
                moveMarkerAndCamera(currentLocation)


            }
            else {
                //log Location as null
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
            savedInstanceState: Bundle?
                             ): View? {
        binding = FragmentRiderBinding.inflate(inflater)
        val viewModel = ViewModelProvider(this).get(MyViewModel::class.java)
        binding.viewModel = viewModel

        //initializing the handler
        handler = Handler(Looper.getMainLooper())
        //initializing the fusedLocationProviderClient
        fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(requireActivity())

        //initializing locationRequest
        locationRequest = LocationRequest().apply {


            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 15000
            fastestInterval = 10000
        }

        //initializing Firestore
        firestore = FirebaseFirestore.getInstance()


        //initialize current user


        currentUserId = FirebaseAuth.getInstance().uid!!

        //Call Uber Button implementation
        binding.callUberButton.setOnClickListener {

            //Uber Request
            if (!isButtonClicked) {
                //create Firestore Geopoint variable from currentLocation
                val geoPoint = GeoPoint(currentLocation.latitude, currentLocation.longitude)


                //create uberRequest object with 3 arguments
                val request = UberRequest(geoPoint, isRequestAccepted, Date())
                Timber.i("originalU-loc $request")
                //save userLocation on firestore
                firestore.collection("UberRequest")
                        .add(request)
                        .addOnSuccessListener {

                            //get the document id for using in deleting the document
                            docID = it.id
                            Toast.makeText(activity, "Uber Requested", Toast.LENGTH_SHORT)
                                    .show()
                        }.addOnFailureListener {

                            Timber.i("Error in saving User's location: $it")
                        }


                binding.callUberButton.text = getString(R.string.cancel_uber_request)
                isButtonClicked = true


                handler.postDelayed({

                                        Timber.i("postDelayed() Triggered from button")
                                        //method that we can run after 2 secs
                                        checkForUpdates()

                                    }, 5000)
            }
            //Uber Request Cancellation
            else {


                //delete document request from firestore
                firestore.collection("UberRequest")
                        .document(docID)
                        .delete()
                        .addOnSuccessListener {

                            Timber.i("$docID deleted")
                            Toast.makeText(activity, "Request Cancelled", Toast.LENGTH_SHORT).show()
                        }


                binding.callUberButton.text = getString(R.string.request_uber)
                isButtonClicked = false
            }
        }


        //log out button implementation
        binding.logOutButton.setOnClickListener {
            findNavController().navigate(RiderFragmentDirections.actionRiderFragmentToLoginFragment())
            FirebaseAuth.getInstance().signOut()


        }

        return binding.root
    }

    private fun checkForUpdates() {


        //check if the request has been accepted by the driver

        //create a document reference
        val docRef = firestore
                .collection("UberRequest")
                .document(docID)

        //use get() to retrieve the document specified by docID variable
        docRef.get(Source.SERVER)
                .addOnSuccessListener { documentSnapshot ->
                    //if documentSnapshot is not null read value of isRequestAccepted
                    if (documentSnapshot != null) {
                        val uberRequest = documentSnapshot.toObject(UberRequest::class.java)

                        //null check on UberRequest object
                        uberRequest?.let {
                            //set the value of isAccepted to server's value
                            isRequestAccepted = uberRequest.accepted
                        }
                    }


                }.addOnFailureListener { Timber.i("Document not found") }



        if (isRequestAccepted) {


            Snackbar.make(
                    binding.root,
                    resources.getString(R.string.driver_on_the_way),
                    Snackbar.LENGTH_SHORT
                         ).show()
            //make callUberButton INVISIBLE
            binding.callUberButton.visibility = View.INVISIBLE
        }

        //handler to call checkForUpdates() after every 5 secs
        handler.postDelayed(

                {
                    Timber.i("postDelayed() Triggered - $docID")

                    checkForUpdates()
                }, 5000
                           )

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
                    Looper.getMainLooper()
                                                              )
        }
        catch (e: SecurityException) {
            //Create a function to request necessary permissions from the app.

        }
    }

    //remove location updates in background
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
        val builder = LatLngBounds.builder()

        //create rider's marker
        val marker = map.addMarker(
                MarkerOptions().position(currentLatLng)
                        .title(resources.getString(R.string.rider_marker))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)))
        //make info window persist on the map
        marker.showInfoWindow()
        builder.include(marker.position)


        //generate bounds
        val bounds = builder.build()

        //CameraUpdateFactory
        val cu = CameraUpdateFactory.newLatLngBounds(bounds, 200)
        map.moveCamera(cu)
        map.animateCamera(CameraUpdateFactory.zoomTo(11f), 2000, null)

    }


}


