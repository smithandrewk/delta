package com.example.deltamobile.onBoarding.screens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.deltamobile.R

class Screen02 : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_screen02, container, false)

        val viewPager = activity?.findViewById<ViewPager2>(R.id.vpOnBoarding)

        // go to next screen on click
        view.findViewById<TextView>(R.id.tvNext02).setOnClickListener{
            viewPager?.currentItem = 2
        }
        // skip boarding if user wants to
        view.findViewById<TextView>(R.id.tvSkip02).setOnClickListener{
            findNavController().navigate(R.id.action_viewPagerFragment_to_dashboard)
        }

        return view
    }
}
