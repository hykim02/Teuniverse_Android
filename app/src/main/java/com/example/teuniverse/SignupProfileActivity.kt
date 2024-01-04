package com.example.teuniverse

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.teuniverse.databinding.ActivityMainBinding

class SignupProfileActivity:AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_profile)

        val nextBtn = findViewById<Button>(R.id.next_btn)
        val backBtn = findViewById<ImageButton>(R.id.back_btn)
        val galleryBtn = findViewById<ImageButton>(R.id.gallery_btn)
        val profileImg = findViewById<ImageView>(R.id.profile_image)

        nextBtn.setOnClickListener {
            val intent = Intent(this, SignupApprovalActivity::class.java)
            startActivity(intent)
            finish()
        }

        backBtn.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}