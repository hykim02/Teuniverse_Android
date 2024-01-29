package com.example.teuniverse

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.teuniverse.databinding.CommunityPostItemBinding

class CommunityPostActivity: AppCompatActivity() {
    private lateinit var binding: CommunityPostItemBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CommunityPostItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.closeBtn.setOnClickListener {
            val intent = Intent(this, CommunityFragment::class.java)
            startActivity(intent)
            finish()
        }
    }
}