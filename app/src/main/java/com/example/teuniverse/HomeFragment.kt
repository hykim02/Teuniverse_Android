package com.example.teuniverse

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.example.teuniverse.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding =  FragmentHomeBinding.inflate(inflater, container, false)

        navigate()

        return binding.root
    }

    private fun navigate() {
        // 투표 화면
        binding.toVote.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_vote)
        }
        binding.toVoteBtn.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_vote)
        }

        // 일정 화면
        binding.toCalendar.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_calendar)
        }
        binding.toCalendarBtn.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_calendar)
        }

        // 미디어 화면
        binding.toMedia.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_media)
        }
        binding.toMediaBtn.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_media)
        }

        // 커뮤니티 화면
        binding.toCommunity.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_community)
        }
        binding.toCommunityBtn.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_community)
        }

        // 프로필
        binding.imgBtnPerson.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_profileFragment2)
        }
    }

}