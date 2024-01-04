package com.example.teuniverse

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class MenuActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        val navigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation_view)

        supportFragmentManager.beginTransaction().replace(R.id.main_container, HomeFragment())
            .commit()

        navigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.fragment_calendar -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, CalendarFragment()).commit()
                }

                R.id.fragment_community -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, CommunityFragment()).commit()
                }

                R.id.fragment_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, HomeFragment()).commit()
                }

                R.id.fragment_media -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, MediaFragment()).commit()
                }

                R.id.fragment_vote -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_container, VoteFragment()).commit()
                }
            }

            true
        }
    }
}

