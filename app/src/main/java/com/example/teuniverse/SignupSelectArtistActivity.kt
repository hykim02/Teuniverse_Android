package com.example.teuniverse

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity

class SignupSelectArtistActivity:AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_select_artist)

        val nextBtn = findViewById<Button>(R.id.next_btn)
        val backBtn = findViewById<ImageButton>(R.id.back_btn)
        val filterSpinner = findViewById<Spinner>(R.id.select_spinner)

        nextBtn.setOnClickListener {
            val intent = Intent(this, SignupEndActivity::class.java)
            startActivity(intent)
            finish()
        }

        backBtn.setOnClickListener{
            val intent = Intent(this, SignupApprovalActivity::class.java)
            startActivity(intent)
            finish()
        }

        var spinnerList = listOf("인기순", "가나다순")
        var spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerList)
        filterSpinner.adapter = spinnerAdapter
    }
}