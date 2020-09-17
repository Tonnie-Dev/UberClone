package com.androidshowtime.uberclone.model

import com.google.firebase.firestore.GeoPoint

data class DriverLocation(val geoPoint: GeoPoint) {

    override fun toString(): String {
        return "DriverLocation(geoPoint=${geoPoint.latitude}, ${geoPoint.longitude})"
    }
}