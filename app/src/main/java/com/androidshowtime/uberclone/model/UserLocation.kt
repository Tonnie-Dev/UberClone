package com.androidshowtime.uberclone.model

import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp

data class UserLocation(private val user: User,
                        private val geoPoint: GeoPoint,
    /*ServerTimestamp is unique to firestore. What it does is that if you pass
    * null to the timestamp when you insert this object into the database it
    * will automatically insert a timestamp of the exact time that it was
    * created*/
                        @ServerTimestamp private val timestamp: String){



}