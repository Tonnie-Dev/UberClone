package com.androidshowtime.uberclone.model

data class User(val uid: String ,val userType:String ){
    override fun toString(): String {
        return "{uid: $uid , userType: $userType}"
    }
}