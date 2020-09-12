package com.androidshowtime.uberclone.driver


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.androidshowtime.uberclone.databinding.FragmentDriverBinding

class DriverFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
                             ): View? {


        val binding = FragmentDriverBinding.inflate(inflater)

        (activity as AppCompatActivity).supportActionBar?.show()
        (activity as AppCompatActivity).supportActionBar?.title = "Nearby Requests"


        val list = mutableListOf<String>("Sue", "Winnie", "Lucy")

        val adapter = ArrayAdapter<String>(requireActivity(), android.R.layout
            .simple_list_item_1,list)

        binding.listView.adapter = adapter




        return binding.root
    }

}