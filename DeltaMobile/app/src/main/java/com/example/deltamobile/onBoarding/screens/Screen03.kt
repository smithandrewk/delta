package com.example.deltamobile.onBoarding.screens

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.example.deltamobile.R

class Screen03 : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.boarding__frag_screen03, container, false)

        // go to home screen on last boarding
        //
        view.findViewById<TextView>(R.id.tvFinal).setOnClickListener{
            findNavController().navigate(R.id.action_viewPagerFragment_to_dashboard)
            // update the on boarding value
            onBoardingFinished()
        }

        return view
    }

    // use shared pref object to save if user has completed boarding or not
    //
    private fun onBoardingFinished(){
        val sharedPref = requireActivity().getSharedPreferences("onBoarding", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putBoolean("Finished",true)
        editor.apply()
    }
}
