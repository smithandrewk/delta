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
        val WAIT_MS:Long = 2000L
        // navigate user from splash frag to other frags after a couple sec
        Handler().postDelayed({
            if(onBoardingFinished()){
                findNavController().navigate(R.id.action_splashFragment_to_dashboard)
            }else{
                // need to board
                findNavController().navigate(R.id.action_splashFragment_to_viewPagerFragment)
            }
        },WAIT_MS)
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.splash__fragment_splash, container, false)
    }

    // check if the user has completed boarding or not
    // if completed, navigate user to home frag instead
    //
    private fun onBoardingFinished():Boolean{
        val sharedPref = requireActivity().getSharedPreferences("onBoarding",Context.MODE_PRIVATE)
        return sharedPref.getBoolean("Finished",false)
    }

}