package com.example.teuniverse

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.teuniverse.databinding.FragmentProfileBinding
import com.google.android.material.tabs.TabLayoutMediator

class ProfileFragment : Fragment() {
    private lateinit var  binding: FragmentProfileBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val profilePagerAdapter = ProfilePagerAdapter(childFragmentManager, lifecycle)
        profilePagerAdapter.addFragment(ProfileCommunityTabFragment(), "커뮤니티")
        profilePagerAdapter.addFragment(ProfileUserTabFragment(), "회원정보")

        binding.tabViewPager.adapter = profilePagerAdapter

        TabLayoutMediator(binding.profileTablayout, binding.tabViewPager) { tab, position ->
            tab.text = profilePagerAdapter.getTitle(position)
        }.attach()
    }
}