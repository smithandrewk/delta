package com.example.deltamobile.onBoarding

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.example.deltamobile.R
// fragment screens
import com.example.deltamobile.onBoarding.screens.Screen01
import com.example.deltamobile.onBoarding.screens.Screen02
import com.example.deltamobile.onBoarding.screens.Screen03

class ViewPagerFragment: Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_view_pager_fragment, container, false)

        val fragmentList = ArrayList<Fragment>()
        // add onboarding screens
        fragmentList.add(Screen01())
        fragmentList.add(Screen02())
        fragmentList.add(Screen03())

        val adapter = ViewPagerAdapter(
            fragmentList,
            requireActivity().supportFragmentManager,
            lifecycle
        )

        view.findViewById<ViewPager2>(R.id.vpOnBoarding).adapter = adapter

        return view
    }
}