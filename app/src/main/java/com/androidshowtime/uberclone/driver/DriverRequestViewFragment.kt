package com.androidshowtime.uberclone.driver


import android.location.Geocoder
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
import androidx.navigation.fragment.findNavController
import com.androidshowtime.uberclone.databinding.FragmentDriverRequestViewBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import timber.log.Timber
import java.util.*
import kotlin.math.roundToInt

class DriverRequestViewFragment : Fragment() {

    //vars
    private lateinit var requestsList: MutableList<String>
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var firestore: FirebaseFirestore
    private lateinit var driverCurrentLocation: Location
    private lateinit var requestsLocationList: MutableList<Location>
    private lateinit var documentID: String
    private lateinit var distanceList: MutableList<Int>
    private var riderDistanceFromDriver: Int = 0

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
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private var locationCallback: LocationCallback = object : LocationCallback() {

        override fun onLocationResult(locationResult: LocationResult?) {
            super.onLocationResult(locationResult)

            if (locationResult != null) {

                locationResult.locations.forEach { driverCurrentLocation = it }

                getAllRideRequests()


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

        //set initial driver's location before the location updates kicks off avoid crashes
        val loc = Location("")
        loc.latitude = 0.0
        loc.longitude = 0.0
        driverCurrentLocation = loc
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
            interval = 9000
            fastestInterval = 8000
        }


        val binding = FragmentDriverRequestViewBinding.inflate(inflater)
        //add title to Action Bar
        (activity as AppCompatActivity).supportActionBar?.show()
        (activity as AppCompatActivity).supportActionBar?.title = "Nearby Requests"

        //initialize requestList and requestGeoPoints Lists
        requestsList = mutableListOf()
        requestsLocationList = mutableListOf()
        distanceList = mutableListOf()

        //initialize Adapter
        adapter = ArrayAdapter<String>(
            requireActivity(), android.R.layout
                .simple_list_item_1, requestsList
                                      )

        binding.listView.adapter = adapter

        binding.listView.setOnItemClickListener { _, _, i, _ ->
            val userLocation = requestsLocationList[i]

            riderDistanceFromDriver = distanceList[i]

            findNavController().navigate(
                DriverRequestViewFragmentDirections
                    .actionDriverRequestViewFragmentToDriverMapFragment
                        (
                        userLocation, driverCurrentLocation,
                        documentID, riderDistanceFromDriver))

        }




        return binding.root
    }

    //startLocationUpdates() to be called once permission has been granted
    private fun startLocationUpdates() {

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

    private fun getAllRideRequests() {

        //get all request documents
        firestore.collection("UserLocation")
            .get()
            .addOnSuccessListener { result ->

                for (requestDocument in result) {


                    val geoPoint = requestDocument.getGeoPoint("geoPoint")!!
                    documentID = requestDocument.id
                    //set userLocation latitude and longitude on Location class
                    val userLocation = Location("")
                    userLocation.latitude = geoPoint.latitude
                    userLocation.longitude = geoPoint.longitude





                    populateListWithRequests(documentID, userLocation)


                }

            }.addOnFailureListener {

                Timber.e(it)
            }

        // clear list
        requestsList.clear()
        adapter.notifyDataSetChanged()


    }


    private fun populateListWithRequests(docId: String, userLocation: Location) {

        //capture user's geoPoint and store in in a list
        requestsLocationList.add(userLocation)

        //obtain address from geoCoding Method
        val address = geoCodingMethod(LatLng(userLocation.latitude, userLocation.longitude))

        //using Location class distanceTo() to calculate distance in km
        val distance = driverCurrentLocation.distanceTo(userLocation) / 1000

        //filter list to include only locations <50 KM
        if (distance <= 50.0) {
            requestsList.add("$address \n ${distance.roundToInt()} KM")
            distanceList.add(distance.roundToInt())
            adapter.notifyDataSetChanged()
            Timber.i("List Size is after population ${requestsList.size}")
        }


    }

    //geoCodingMethod()
    private fun geoCodingMethod(latLng: LatLng): String {

        //initialize Geocoder
        val geoCoder = Geocoder(activity, Locale.getDefault())
        var address = ""
        try {
            val listOfAddresses = geoCoder.getFromLocation(latLng.latitude, latLng.longitude, 1)


            //checking size of the list to ensure it has at least 1 item
            if (listOfAddresses.size > 0) {
                //null check on listOfAddresses
                listOfAddresses?.let {


                    if (it[0].locality != null) {
                        if (it[0].subLocality != null) {

                            if (it[0].thoroughfare != null) {

                                address += listOfAddresses[0].thoroughfare + ", "
                            }

                            address += listOfAddresses[0].subLocality + " - "
                        }



                        address += listOfAddresses[0].locality
                    }
                }
            }


        } catch (e: Exception) {
            e.printStackTrace()
        }


        if (address.isEmpty()) {

            address += "Unnamed Place"
        }
        return address
    }


}

/*  val user: User = Gson().fromJson(requestDocument.get("user").toString(),
                                                    User::class.java)*/
