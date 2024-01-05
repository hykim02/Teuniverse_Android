package com.example.teuniverse

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory



class SignupSelectArtistActivity:AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_select_artist)

        val nextBtn = findViewById<Button>(R.id.next_btn)
        val backBtn = findViewById<ImageButton>(R.id.back_btn)
        val filterSpinner = findViewById<Spinner>(R.id.select_spinner)
        val searchTxt = findViewById<EditText>(R.id.search_txt)

        searchTxt.hint = "아티스트를 검색하세요"

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

        var spinnerList = arrayOf("인기순", "가나다순")
        var spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, spinnerList)
        filterSpinner.adapter = spinnerAdapter
        filterSpinner.setSelection(0)
    }
}