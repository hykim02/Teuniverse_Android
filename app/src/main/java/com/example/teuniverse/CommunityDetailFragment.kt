package com.example.teuniverse

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.navigation.NavHost
import androidx.navigation.fragment.NavHostFragment
import com.example.teuniverse.databinding.FragmentCommunityDetailBinding

class CommunityDetailFragment : Fragment() {

    private lateinit var binding: FragmentCommunityDetailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCommunityDetailBinding.inflate(inflater, container, false)
        binding.backBtnDetail.setOnClickListener {
            NavHostFragment.findNavController(this).navigate(R.id.action_navigation_communityDetail_to_navigation_community)
        }
        return binding.root
    }

}