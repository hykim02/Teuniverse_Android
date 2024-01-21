package com.example.teuniverse

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MenuActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val navigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation_view)

        supportFragmentManager.beginTransaction().replace(R.id.nav_host_fragment, HomeFragment())
            .commit()

        // navigation 설정
//        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
//        val navController = navHostFragment.navController
//        navController.setGraph(R.navigation.navigation_bar)
//        navigationView.setupWithNavController(navController)

        navigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.fragment_calendar -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment, CalendarFragment()).commit()
                }

                R.id.fragment_community -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment, CommunityFragment()).commit()
                }

                R.id.fragment_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment, HomeFragment()).commit()
                }

                R.id.fragment_media -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment, MediaFragment()).commit()
                }

                R.id.fragment_vote -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment, VoteFragment()).commit()
                }
            }

            true
        }
    }
}

