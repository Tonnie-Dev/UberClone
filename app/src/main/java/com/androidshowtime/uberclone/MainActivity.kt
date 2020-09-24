package com.androidshowtime.uberclone

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import timber.log.Timber

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Timber.plant(Timber.DebugTree())


        // use nav controller to add Up-Button to the app
        val navController = this.findNavController(R.id.nav_host_fragment)

        // link the navigation controller to the app bar
        NavigationUI.setupActionBarWithNavController(this, navController)
    }

    //override onSupportNavigateUp() to call navigateUp() in the navigation controller
    override fun onSupportNavigateUp(): Boolean {

        val navController = this.findNavController(R.id.nav_host_fragment)
        return navController.navigateUp()
    }
}