package com.example.deltamobile.DashboardTabNav

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

// Adapter for view pager
//
class VPAdapter(fm: FragmentManager, behavior:Int):FragmentPagerAdapter(fm,behavior){
    // list of fragments
    private val arrlistFrags = ArrayList<Fragment>()
    // list of fragment titles
    private val arrlistFragTitles = ArrayList<String>()

    override fun getCount(): Int {
        return this.arrlistFrags.size
    }

    override fun getItem(position: Int): Fragment {
        return this.arrlistFrags[position]
    }

    override fun getPageTitle(position:Int):String{
        // return empty to only give icon
//        return this.arrlistFragTitles[position]
        return ""
    }


    // add frags from main activity to array list
    fun addFragment(fragment: Fragment,title:String){
        this.arrlistFrags.add(fragment)
        this.arrlistFragTitles.add(title)
    }

}