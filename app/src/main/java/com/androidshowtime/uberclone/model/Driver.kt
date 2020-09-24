package com.androidshowtime.uberclone.model

import com.google.firebase.firestore.GeoPoint

data class Driver(val driverActivationId:String? = null, val geoPoint: GeoPoint? = null) {

    override fun toString(): String {
        return "DriverLocation(uid = $driverActivationId geoPoint=${geoPoint?.latitude}, ${geoPoint?.longitude})"
    }
}