package com.androidshowtime.uberclone.driver


import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.androidshowtime.uberclone.R
import com.androidshowtime.uberclone.databinding.FragmentDriverMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.firestore.FirebaseFirestore
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
    private lateinit var docID: String


    private val callback = OnMapReadyCallback { googleMap ->

        map = googleMap

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
                    .snippet(snippet)

                    .icon(BitmapDescriptorFactory.fromBitmap(bitmap)))

        //show the infoWindow permanently
        riderMarker.showInfoWindow()
        Timber.i("${args.riderDistanceFromDriver} KM Away")
        markers.add(riderMarker)

        // .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory
        // .HUE_RED))))


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
        map.animateCamera(CameraUpdateFactory.zoomTo(10f), 2000, null)

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
        //retrieve docID
        val docID = args.docID
        //initialize markers list
        markers = mutableListOf()


        //acceptButton onClick implementation
        binding.acceptButton.setOnClickListener {

            firestore = FirebaseFirestore.getInstance()


            //update the requestAccepted field value in firestore
            val docRef = firestore
                .collection("UserLocation")
                .document(docID)


            docRef.update("requestAccepted", true).addOnSuccessListener {

                Timber.i("update done - $docID")
            }.addOnFailureListener{

                Timber.i("update Failed!!")
            }

            /*create the display map parameters to form the map URL(use %2C for URL encoding and escape commas)*/
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
                        PackageManager.MATCH_DEFAULT_ONLY)
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


    private fun showPath(latLngList: MutableList<LatLng>) {


        val polylineOptions = PolylineOptions()
        polylineOptions.color(Color.MAGENTA)
        polylineOptions.width(20f)
        polylineOptions.addAll(latLngList)
        magentaPolyline?.tag = "A"

        magentaPolyline?.isClickable = true
        magentaPolyline = map.addPolyline(polylineOptions)


    }


}