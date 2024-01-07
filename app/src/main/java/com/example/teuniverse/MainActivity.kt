package com.example.teuniverse

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val kakaoLogin = findViewById<ImageButton>(R.id.kakao_login)
        val naverLogin = findViewById<ImageButton>(R.id.naver_login)

        kakaoLogin.setOnClickListener{
            val intent = Intent(this, SignupSelectArtistActivity::class.java)
            startActivity(intent)
            finish()
        }

        naverLogin.setOnClickListener{
            val intent = Intent(this, SignupProfileActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}