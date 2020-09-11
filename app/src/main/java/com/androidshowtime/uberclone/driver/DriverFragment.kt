package com.androidshowtime.uberclone.driver


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.androidshowtime.uberclone.databinding.FragmentDriverBinding

class DriverFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
                             ): View? {




        val binding =FragmentDriverBinding.inflate(inflater)
        return binding.root
    }

}