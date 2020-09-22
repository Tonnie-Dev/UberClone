package com.androidshowtime.uberclone.model

import com.google.firebase.firestore.GeoPoint

data class DriverLocation(val uid:String = "", val geoPoint: GeoPoint? = null) {

    override fun toString(): String {
        return "DriverLocation(uid = ${uid} geoPoint=${geoPoint?.latitude}, ${geoPoint?.longitude})"
    }
}