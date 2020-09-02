package com.androidshowtime.uberclone

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.androidshowtime.uberclone.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import timber.log.Timber


class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        auth = FirebaseAuth.getInstance()
        signInAnonymously()
//obtain reference to Database

        val binding = FragmentLoginBinding.inflate(inflater)

        db = FirebaseDatabase.getInstance()


//hide actionBar
        (activity as AppCompatActivity).supportActionBar?.hide()

        val currentUser = auth.currentUser

        if (currentUser != null) {
            Timber.i("the user is ${currentUser.uid}")
        }
        else{

            Timber.i("user is null null")
        }
        //button onClickListener
        binding.loginButton.setOnClickListener {
            //set the user type as rider by default
            var userType = "Rider"
            if (binding.switch1.isChecked) {

                userType = "Driver"
            }



            val userMap = mapOf("User Type" to userType)
            if (currentUser != null) {
                db.reference.child("Users").child(currentUser.uid).setValue(userMap)
            }


            when(userType){

                "Rider" -> {


                    findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToRiderFragment()) }
                "Driver" -> {


                    findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToDriverFragment())

                }




            }
        }


        /* ref.addValueEventListener(object : ValueEventListener {
             override fun onDataChange(snapshot: DataSnapshot) {

             }

             override fun onCancelled(error: DatabaseError) {
                 //Failed to read value

                 Timber.i("Failed to read value: $error")
             }
         })*/




        return binding.root
    }


    private fun signInAnonymously() {
//call signInAnonymously to sign in as an anonymous user
        auth.signInAnonymously().addOnCompleteListener {


            if (it.isSuccessful) {

                Toast.makeText(activity, "Anonymous Login Successful", Toast.LENGTH_SHORT).show()
                Timber.i("Anonymous Login Successful")

            } else {

                Toast.makeText(activity, "Anonymous Login Failed", Toast.LENGTH_SHORT).show()
                Timber.i("Anonymous Login Failed")

            }


        }

    }


}