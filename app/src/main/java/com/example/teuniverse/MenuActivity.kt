package com.example.teuniverse

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.teuniverse.databinding.ActivityMenuBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MenuActivity: AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.fragment_home,
                R.id.fragment_calendar,
                R.id.fragment_community,
                R.id.fragment_vote,
                R.id.fragment_media
            )
        )

        binding.bottomNavigationView.setupWithNavController(navController)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onNavigateUp(): Boolean = navController.navigateUp() || super.onNavigateUp()


    // 메뉴 액티비티의 NavController
//    private lateinit var menuNavController: NavController
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_menu)
//
//        // 하단 네비게이션 뷰 초기화
//        val navigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
//        supportFragmentManager.beginTransaction().replace(R.id.nav_host_fragment, HomeFragment())
//            .commit()
//
//        navigationView.setOnItemSelectedListener {
//            when (it.itemId) {
//                R.id.fragment_calendar -> {
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.nav_host_fragment, CalendarFragment()).commit()
//                }
//
//                R.id.fragment_community -> {
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.nav_host_fragment, CommunityFragment()).commit()
//                    //val currentNavController = findNavController()
//                    //currentNavController.navigate(R.id.navigation_community)
//                }
//
//                R.id.fragment_home -> {
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.nav_host_fragment, HomeFragment()).commit()
//
//                }
//
//                R.id.fragment_media -> {
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.nav_host_fragment, MediaFragment()).commit()
//
//                }
//
//                R.id.fragment_vote -> {
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.nav_host_fragment, VoteFragment()).commit()
//                }
//            }
//
//            true
//        }
//    }
}

