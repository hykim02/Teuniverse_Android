package com.example.teuniverse

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.teuniverse.databinding.FragmentCalendarBinding
import com.example.teuniverse.databinding.FragmentNewProfileBinding

class NewProfileFragment : Fragment() {
    private lateinit var  binding: FragmentNewProfileBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNewProfileBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }



}