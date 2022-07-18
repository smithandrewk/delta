package com.example.deltamobile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.deltamobile.DashboardTabNav.*
import com.google.android.material.tabs.TabLayout

class Dashboard : AppCompatActivity() {

    private lateinit var tlTabs:TabLayout
    private lateinit var vpPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dashboard__base)

        // set up tab nav
        this.tlTabs = findViewById(R.id.tlNavTabs)
        this.vpPager = findViewById(R.id.vpNavPager)

        this.tlTabs.setupWithViewPager(vpPager)




        var vpAdapter = VPAdapter(supportFragmentManager,FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)

        // add frags
        vpAdapter.addFragment(HomeFragment(),"Home")
        vpAdapter.addFragment(StatsFragment(),"Stats")
        vpAdapter.addFragment(FavoritesFragment(),"Favorites")
        vpAdapter.addFragment(SettingsFragment(),"Settings")

        vpPager.adapter = vpAdapter

        /*
        Adding Images:
        Note that order is important!
         */

        // put icons in
        //
        var icons = ArrayList<Int>()
        // in order of left to right on tabs
        // home
        icons.add(R.drawable.ic_baseline_home_24)
        // stats
        icons.add(R.drawable.ic_baseline_cloud_upload_24)
        // favs
        icons.add(R.drawable.ic_baseline_favorite_border_24)
        // settings
        icons.add(R.drawable.ic_baseline_settings_24)
        for(i in 0..tlTabs.tabCount){
            tlTabs.getTabAt(i)?.setIcon(icons[i])
        }

        supportActionBar?.hide()
    }
}