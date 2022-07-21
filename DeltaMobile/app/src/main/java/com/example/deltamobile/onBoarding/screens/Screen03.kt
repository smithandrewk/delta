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
import com.example.deltamobile.spMAIN_ACTIVITY__BOARDING_COMPLETE
import com.example.deltamobile.spnameMAIN_ACTIVITY__ON_BOARDING

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
            // update the on boarding value
            val sharedPref = requireActivity().getSharedPreferences(spnameMAIN_ACTIVITY__ON_BOARDING, Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putBoolean(spMAIN_ACTIVITY__BOARDING_COMPLETE,true)
            editor.apply()
            findNavController().navigate(R.id.action_viewPagerFragment_to_welcomescreen)
        }

        return view
    }
}
