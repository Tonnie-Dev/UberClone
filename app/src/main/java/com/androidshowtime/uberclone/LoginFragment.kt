package com.androidshowtime.uberclone

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.androidshowtime.uberclone.databinding.FragmentLoginBinding


class LoginFragment : Fragment() {




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentLoginBinding.inflate(inflater)




        (activity as AppCompatActivity).supportActionBar?.hide()
        return binding.root
    }


}