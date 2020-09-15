package com.androidshowtime.uberclone.driver

import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.androidshowtime.uberclone.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import timber.log.Timber


class DriverMapFragment : Fragment() {
    //vals
    private val args: DriverMapFragmentArgs by navArgs()

    //vars
    private lateinit var map: GoogleMap
    private lateinit var markers: MutableList<Marker>

    private val callback = OnMapReadyCallback { googleMap ->

        map = googleMap

        //retrieve driver and rider location from navigation arguments
        val riderLocation = args.userLocation
        val driverLocation = args.driverLocation

        //obtain driver and rider co-ordinates
        val riderLatLng = LatLng(riderLocation.latitude, riderLocation.longitude)
        val driverLatLng = LatLng(driverLocation.latitude, driverLocation.longitude)


        markers.add(
            map.addMarker(
                MarkerOptions().position(riderLatLng)
                    .title("Driver")
                    .snippet("Driver")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))))


        markers.add(
            map.addMarker(
                MarkerOptions().position(driverLatLng)
                    .title("Driver")
                    .snippet("Driver")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))))

        //create builder
        val builder = LatLngBounds.builder()

        //loop through the markes array
        for (marker in markers) {

            builder.include(marker.position)
        }
        //generate a bound
        val bounds = builder.build()

        //set a 200 pixels padding from the edge of the screen
        val cu = CameraUpdateFactory.newLatLngBounds(bounds,200)

        //move and animate the camera
        map.moveCamera(cu)
        map.animateCamera(cu)

        // showDriverLocation(driverLocation)
        //showRiderLocation(riderLocation)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
                             ): View? {
        //initialize markers list

        markers = mutableListOf()
        Timber.i("The passed Geopoint is ${args.userLocation}")

        return inflater.inflate(R.layout.fragment_driver_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }


    fun showDriverLocation(driverLocation: Location) {

        //clear all markers
        map.clear()
        //obtain LatLng from driverLocation
        val latLng = LatLng(driverLocation.latitude, driverLocation.longitude)

        //move camera to driver's location
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))

        //move pointer to driver's location
        map.addMarker(
            MarkerOptions().position(latLng)
                .title("Driver")
                .snippet("Driver")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                     )

    }


    private fun showRiderLocation(riderLocation: Location) {
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.rider_icon)
        val latLng = LatLng(riderLocation.latitude, riderLocation.longitude)

        //move camera to rider's location
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))

        //move pointer to rider's location
        map.addMarker(
            MarkerOptions().position(latLng)
                .title("Rider")
                .snippet("Rider")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                     ).isVisible

    }
}