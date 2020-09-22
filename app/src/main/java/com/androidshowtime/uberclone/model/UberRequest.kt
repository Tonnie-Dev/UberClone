package com.androidshowtime.uberclone.model

import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class UberRequest( val docId:String  = "",
                        val geoPoint: GeoPoint? = null,

                       //don't ues 'is' prefix on boolean property on firebase
                       val accepted:Boolean = false,


                       @ServerTimestamp
                       val timestamp: Date? = null)


/*
val geoPoint: GeoPoint? = null,

//add a variable to see if the request is accepted
val isReqAccepted:Int = 0,
*/
/*ServerTimestamp is unique to firestore. What it does is that if you pass
* null to the timestamp when you insert this object into the database it
* will automatically insert a timestamp of the exact time that it was
* created*//*

@ServerTimestamp
val timestamp: Date? = null*/
