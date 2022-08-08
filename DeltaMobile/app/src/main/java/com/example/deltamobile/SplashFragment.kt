package com.example.deltamobile

import android.content.Context
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController

class SplashFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // time to show the splash screen
        val longSplashLength:Long = 2000L

        // navigate user from splash frag to other frags after a couple sec
        Handler().postDelayed({
            if(onBoardingFinished()){
                if(onUpdateViewingFinished()){
                    // complete with seeing updates, go straight to dashboard
                    findNavController().navigate(R.id.action_splashFragment_to_dashboard)
                }
                else{
                    // need to view updates
                    findNavController().navigate(R.id.action_splashFragment_to_welcomescreen)
                }
            }else{
                // need to board
                findNavController().navigate(R.id.action_splashFragment_to_viewPagerFragment)
            }
        },longSplashLength)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.splash__fragment_splash, container, false)
    }

    ////
    // check if user has viewed updates or not
    //
    private fun onUpdateViewingFinished():Boolean{
        val sharedPref = requireActivity().getSharedPreferences(spnameMAIN_ACTIVITY__UPDATES_VIEWED,Context.MODE_PRIVATE)
        return sharedPref.getBoolean(spnameMAIN_ACTIVITY__UPDATES_VIEWED,false)
    }

    // check if the user has completed boarding or not
    // if completed, navigate user to home frag instead
    //
    private fun onBoardingFinished():Boolean{
        val sharedPref = requireActivity().getSharedPreferences(spnameMAIN_ACTIVITY__ON_BOARDING,Context.MODE_PRIVATE)
        return sharedPref.getBoolean(spMAIN_ACTIVITY__BOARDING_COMPLETE,false)
    }

}