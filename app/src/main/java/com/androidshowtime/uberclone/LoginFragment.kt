package com.androidshowtime.uberclone

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.androidshowtime.uberclone.databinding.FragmentLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import timber.log.Timber


class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentLoginBinding.inflate(inflater)
        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
        signInAnonymously()

//hide actionBar
        (activity as AppCompatActivity).supportActionBar?.hide()




        //button onClickListener
        binding.loginButton.setOnClickListener {
            //set the user type as rider by default
            var userType = "Rider"
            if (binding.switch1.isChecked) {

                userType = "Driver"
            }

        }

        //obtain reference to Database

        val ref  = db.reference.child("Users")
        ref.setValue(userType)

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