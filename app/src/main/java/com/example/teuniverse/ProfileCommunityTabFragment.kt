package com.example.teuniverse

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.teuniverse.databinding.FragmentProfileCommunityTabBinding

class ProfileCommunityTabFragment : Fragment() {
    private lateinit var binding: FragmentProfileCommunityTabBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileCommunityTabBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }
}