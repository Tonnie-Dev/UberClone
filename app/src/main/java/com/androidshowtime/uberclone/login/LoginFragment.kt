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
    //declaring firebase components
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var firestore: FirebaseFirestore
    private var currentUser: FirebaseUser? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
                             ): View? {
        val binding = FragmentLoginBinding.inflate(inflater)
        auth = FirebaseAuth.getInstance()
        signInAnonymously()


        //obtain reference to Database
        db = FirebaseDatabase.getInstance()

        //obtain reference to Firestore
        firestore = FirebaseFirestore.getInstance()


        //hide actionBar
        (activity as AppCompatActivity).supportActionBar?.hide()
        currentUser = auth.currentUser

        //button onClickListener
        binding.loginButton.setOnClickListener {
            //set the user type as rider by default
            var userType = "Rider"
            if (binding.switch1.isChecked) {

                userType = "Driver"
            }

            // create a new user with usertype for Realtime Dabase
            val userMapRTD = mapOf("User Type" to userType)
            currentUser?.uid?.let { it1 ->
                db.reference.child("Users")
                    .child(it1)
                    .setValue(userMapRTD)
            }

            // create a new user with userType for Firestore
            val userMapCF = mapOf("User Type" to userType)

            // Add a new document with an auto-generated  doc ID
            firestore.collection("users") //collection
                .add(userMapCF) // document with auto ID
                .addOnSuccessListener {

                    Timber.i("DocumentSnapshot added with ID ${it.id}")
                }
                .addOnFailureListener {

                    Timber.i("Error Encountered - $it")
                }


            currentUser?.uid?.let { uid ->
                firestore.collection("users") //collection
                    .document(uid)           //document ID specified
                    .set(userMapCF).addOnSuccessListener {

                        Timber.i("Document created")
                    }
                    .addOnFailureListener {

                        Timber.i("Error Encountered - $it")
                    }
            }


            when (userType) {
                //if userType is is a rider, navigate to RiderFragment
                "Rider" -> {


                    findNavController().navigate(
                        LoginFragmentDirections.actionLoginFragmentToRiderFragment(userType)
                                                )
                }

                //if userType is is a Driver, navigate to DriverFragment
                "Driver" -> {


                    findNavController().navigate(
                        LoginFragmentDirections.actionLoginFragmentToDriverFragment()
                                                )

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

                    Toast.makeText(activity, "Anonymous Login Successful", Toast.LENGTH_SHORT)
                        .show()

                } else {

                    Toast.makeText(activity, "Anonymous Login Failed", Toast.LENGTH_SHORT)
                        .show()
                }


            }

    }


}