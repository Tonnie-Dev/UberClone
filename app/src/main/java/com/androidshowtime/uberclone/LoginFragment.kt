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
import timber.log.Timber


class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth





    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentLoginBinding.inflate(inflater)
        auth = FirebaseAuth.getInstance()
        signInAnonymously()


        (activity as AppCompatActivity).supportActionBar?.hide()


        Timber.i("Switch Value:, ${binding.switch1.isChecked}")
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