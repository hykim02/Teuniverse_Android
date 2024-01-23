package com.example.teuniverse

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.example.teuniverse.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =  FragmentHomeBinding.inflate(inflater, container, false)

//        binding.homeBtn.setOnClickListener {
//            NavHostFragment.findNavController(this).navigate(R.id.action_navigation_home_to_navigation_vote2)
//        }

        val navController = findNavController()
        Log.d("HomeFragment 현재 위치",navController.toString())
        Log.d("HomeFragment 현재 위치2", navController.currentDestination?.id.toString())
        return binding.root
    }

}