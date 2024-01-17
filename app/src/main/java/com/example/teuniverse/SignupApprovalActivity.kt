package com.example.teuniverse

import android.content.Intent
import android.media.Image
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SignupApprovalActivity:AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_approval)

        val nextBtn = findViewById<Button>(R.id.next_btn)
        val backBtn = findViewById<ImageButton>(R.id.back_btn)
        val chk1 = findViewById<CheckBox>(R.id.checkBox)
        val chk2 = findViewById<CheckBox>(R.id.checkBox2)
        val chk4 = findViewById<CheckBox>(R.id.checkBox4)

        nextBtn.setOnClickListener {
            if(chk1.isChecked && chk2.isChecked){
                val intent = Intent(this, SignupSelectArtistActivity::class.java)
                startActivity(intent)
                finish()
            } else{
                Toast.makeText(this,"필수동의 항목을 체크해주세요", Toast.LENGTH_SHORT).show()
            }

        }

        backBtn.setOnClickListener{
            val intent = Intent(this, SignupProfileActivity::class.java)
            startActivity(intent)
            finish()
        }


    }
}