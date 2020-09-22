package com.androidshowtime.uberclone.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.androidshowtime.uberclone.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import timber.log.Timber


class LoginFragment : Fragment() {
    //vars
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var firestore: FirebaseFirestore
    private var currentUser: FirebaseUser? = null
    private lateinit var uid:String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
                             ): View? {
        val binding = FragmentLoginBinding.inflate(inflater)
        auth = FirebaseAuth.getInstance()
        signInAnonymously()
        currentUser = auth.currentUser
        uid = currentUser?.uid.toString()

        //obtain reference to Database
        db = FirebaseDatabase.getInstance()

        //obtain reference to Firestore
        firestore = FirebaseFirestore.getInstance()


        //hide actionBar
        (activity as AppCompatActivity).supportActionBar?.hide()


        //button onClickListener
        binding.loginButton.setOnClickListener {
            //set the user type as rider by default
            var userType = "Rider"
            if (binding.switch1.isChecked) {

                userType = "Driver"
            }





            when (userType) {
                //if userType is is a rider, navigate to RiderFragment
                "Rider" -> {


                    findNavController().navigate(
                        LoginFragmentDirections.actionLoginFragmentToRiderFragment(uid))
                }

                //if userType is is a Driver, navigate to DriverFragment
                "Driver" -> {


                    findNavController().navigate(
                        LoginFragmentDirections.actionLoginFragmentToDriverFragment(uid))

                }


            }
        }






        return binding.root
    }

    //sign in anonymously
    private fun signInAnonymously() {
        //call signInAnonymously to sign in as an anonymous user
        auth.signInAnonymously()
            .addOnCompleteListener {


                if (it.isSuccessful) {


                    Timber.i("The Current User is - ${currentUser?.uid}")
                    Toast.makeText(activity, "Anonymous Login Successful", Toast.LENGTH_SHORT)
                        .show()

                } else {

                    Toast.makeText(activity, "Anonymous Login Failed", Toast.LENGTH_SHORT)
                        .show()
                }


            }

    }


}