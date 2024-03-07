package com.example.teuniverse

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
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
        setContentView(binding.root)

        // navController 초기화
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // 하단바와 navController 연결
        binding.bottomNavigationView.setupWithNavController(navController)

        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.fragment_home -> navController.navigate(R.id.navigation_home)
                R.id.fragment_community -> navController.navigate(R.id.navigation_community)
                R.id.fragment_vote -> navController.navigate(R.id.navigation_vote)
                R.id.fragment_calendar -> navController.navigate(R.id.navigation_calendar)
                R.id.fragment_media -> navController.navigate(R.id.navigation_media)
            }
            true
        }

        if (intent.getBooleanExtra("goToProfileFragment", false)) {
            // 프래그먼트 트랜잭션 시작
            val transaction = supportFragmentManager.beginTransaction()
            // 프래그먼트 인스턴스 찾기
            val profileFragment = supportFragmentManager.findFragmentByTag("profileFragment") as? ProfileFragment

            if (profileFragment == null) {
                // 프래그먼트가 없는 경우 새로 생성
                val newProfileFragment = ProfileFragment()
                transaction.replace(R.id.nav_host_fragment, newProfileFragment, "profileFragment")
            } else {
                // 프래그먼트가 이미 있는 경우 해당 프래그먼트로 이동
                transaction.replace(R.id.nav_host_fragment, profileFragment)
            }
            // 트랜잭션 커밋
            transaction.commit()
        } else if (intent.getBooleanExtra("goToCommunityFragment", false)) {
            // 프래그먼트 트랜잭션 시작
            val transaction = supportFragmentManager.beginTransaction()
            // 프래그먼트 인스턴스 찾기
            val communityFragment = supportFragmentManager.findFragmentByTag("communityFragment") as? CommunityFragment

            if (communityFragment == null) {
                // 프래그먼트가 없는 경우 새로 생성
                val newCommunityFragment = CommunityFragment()
                transaction.replace(R.id.nav_host_fragment, newCommunityFragment, "communityFragment")
            } else {
                // 프래그먼트가 이미 있는 경우 해당 프래그먼트로 이동
                transaction.replace(R.id.nav_host_fragment, communityFragment)
            }
            // 트랜잭션 커밋
            transaction.commit()
        }


    }
}

