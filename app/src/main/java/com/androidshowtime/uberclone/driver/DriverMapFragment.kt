package com.androidshowtime.uberclone.driver


import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.androidshowtime.uberclone.R
import com.androidshowtime.uberclone.databinding.FragmentDriverMapBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import timber.log.Timber


class DriverMapFragment : Fragment() {
    //vals
    private val args: DriverMapFragmentArgs by navArgs()

    //vars
    private lateinit var map: GoogleMap
    private lateinit var markers: MutableList<Marker>
    private lateinit var riderLocation: Location
    private lateinit var driverLocation: Location
    private var magentaPolyline: Polyline? = null
    private lateinit var firestore: FirebaseFirestore
    private lateinit var driverDocId: String

    //location components
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private var locationCallback = object : LocationCallback() {
        //override onLocationResult and set it to driver's current location
        override fun onLocationResult(locationResult: LocationResult?) {
            super.onLocationResult(locationResult)

            if (locationResult != null) {
                driverLocation = locationResult.lastLocation



              //update driver location on firestore

              updateDriverLocationOnFirestore()

            }

        }


    }

    //if permission is granted start updates otherwise notify user
    private val locationPermission =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {

                if (it) {

                    startLocationUpdates()
                }
                else {
                    //notify user via Toast
                    Toast.makeText(activity, "Location Permission Needed", Toast.LENGTH_SHORT)
                            .show()
                }

            }


    private val callback = OnMapReadyCallback { googleMap ->

        map = googleMap

        //request for permission
        locationPermission.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)


        //retrieve driver and rider location from navigation arguments
        riderLocation = args.userLocation
        driverLocation = args.driverLocation

        //obtain driver and rider co-ordinates
        val riderLatLng = LatLng(riderLocation.latitude, riderLocation.longitude)
        val driverLatLng = LatLng(driverLocation.latitude, driverLocation.longitude)
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.rider)

        //add snippet to show distance from the driver
        val snippet = "${args.riderDistanceFromDriver} KM away"


        //rider marker
        val riderMarker = map.addMarker(
                MarkerOptions().position(riderLatLng)
                        .title("Rider")
                        .flat(true)
                        .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                                       )

        //show the infoWindow persistently on the icon
        riderMarker.showInfoWindow()

        markers.add(riderMarker)


        markers.add(
                map.addMarker(
                        MarkerOptions().position(driverLatLng)
                                .title("Driver")
                                .snippet("Driver")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))))

        //create builder
        val builder = LatLngBounds.builder()

        //loop through the markers list
        for (marker in markers) {

            builder.include(marker.position)
        }
        //generate a bound
        val bounds = builder.build()

        //set a 150 pixels padding from the edge of the screen
        val cu = CameraUpdateFactory.newLatLngBounds(bounds, 150)


        //move and animate the camera
        map.moveCamera(cu)
        map.animateCamera(CameraUpdateFactory.zoomTo(11f), 2000, null)


        //draw a thin polygon
        val latLngList = mutableListOf(riderLatLng, driverLatLng)
        showPath(latLngList)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
                             ): View? {

        //create binding
        val binding = FragmentDriverMapBinding.inflate(inflater)
                //obtain driverDocId from navigation arguments
        driverDocId = args.driverDocId
//initialize firestore

        firestore = FirebaseFirestore.getInstance()
        //retrieve docID
        val userDocId = args.userDocId
        //initialize markers list
        markers = mutableListOf()


        //initialize location components
        fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(requireActivity())
        locationRequest = LocationRequest().apply {

            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 5000
            fastestInterval = 3000
        }

        //acceptButton onClick implementation
        binding.acceptButton.setOnClickListener {



            //update rider's requestAccepted field value in firestore
            val userRef = firestore
                    .collection("UberRequest")
                    .document(userDocId)


            userRef.update("accepted", true).addOnSuccessListener {

                Timber.i("rider's request Accepted")

            }.addOnFailureListener {

                Timber.i("Rider's request acceptance failed - $it")
            }



            //Tie driver to the rider's request
            val driverRef = firestore
                    .collection("Driver")
                    .document(driverDocId)
            //update requestCode to match rider's code
            driverRef.update("driverActivationId", userDocId)

            /*create the display map parameters to form the map URL(use %2C for URL encoding and
            to escape commas)*/
            val origin = "origin=${driverLocation.latitude}%2C${driverLocation.longitude}&"
            val destination = "destination=${riderLocation.latitude}%2C${riderLocation.longitude}&"
            val travelMode = "travelmode=driving"

            //combine parameters into one parameter
            val parameters = origin + destination + travelMode

            //display map URI
            val directions = "https://www.google.com/maps/dir/?api=1&$parameters"


            // Build the intent
            val location = Uri.parse(directions)
            val mapIntent = Intent(Intent.ACTION_VIEW, location)


            // Verify it resolves
            val activities: List<ResolveInfo> =
                    requireActivity().packageManager.queryIntentActivities(
                            mapIntent,
                            PackageManager.MATCH_DEFAULT_ONLY
                                                                          )
            val isIntentSafe: Boolean = activities.isNotEmpty()

            // Start an activity if it's safe
            if (isIntentSafe) {
                startActivity(mapIntent)
            }

            startActivity(mapIntent)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

//draw polyline using polylineOptions
    private fun showPath(latLngList: MutableList<LatLng>) {
        val polylineOptions = PolylineOptions()
        polylineOptions.color(Color.MAGENTA)
        polylineOptions.width(3f)
        polylineOptions.addAll(latLngList)
        magentaPolyline = map.addPolyline(polylineOptions)

    }

//start updates
    private fun startLocationUpdates() {


        try {

            //request for Location Updates
            fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest, locationCallback,
                    Looper.getMainLooper())
        }
        catch (e: SecurityException) {


        }
    }


    fun updateDriverLocationOnFirestore() {

        //obtain Geopoint from driver's location
        val driverGeoPoint = GeoPoint(driverLocation.latitude, driverLocation.longitude)

        //update location on firestore
        val driverRef = firestore.collection("Driver").document(driverDocId)

        driverRef.update("geoPoint", driverGeoPoint)
                .addOnSuccessListener { Timber.i("Driver's location updated on firestore") }
                .addOnFailureListener { Timber.i("Error $it") }
    }
}