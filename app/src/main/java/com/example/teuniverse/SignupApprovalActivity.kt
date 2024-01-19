package com.example.teuniverse

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SignupApprovalActivity:AppCompatActivity() {

    private lateinit var nextBtn: Button
    private lateinit var backBtn: ImageButton
    private lateinit var chk1: CheckBox
    private lateinit var chk2: CheckBox
    private lateinit var chk4: CheckBox

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_approval)

        nextBtn = findViewById(R.id.next_btn)
        backBtn = findViewById(R.id.back_btn_detail)
        chk1 = findViewById(R.id.checkBox)
        chk2 = findViewById(R.id.checkBox2)
        chk4 = findViewById(R.id.checkBox4)

        setCheckBoxListeners()

        nextBtn.setOnClickListener {
            if(chk1.isChecked && chk2.isChecked && chk4.isChecked){
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

    private fun setCheckBoxListeners() {
        chk1.setOnCheckedChangeListener { _, _ ->
            checkAllCheckBoxes()
        }

        chk2.setOnCheckedChangeListener { _, _ ->
            checkAllCheckBoxes()
        }

        chk4.setOnCheckedChangeListener { _, _ ->
            checkAllCheckBoxes()
        }
    }

    private fun checkAllCheckBoxes() {
        // 모든 체크박스가 선택되었는지 확인
        val allChecked = chk1.isChecked && chk2.isChecked && chk4.isChecked
        // 버튼의 색상 변경
        if (allChecked) {
            nextBtn.setBackgroundColor(Color.parseColor("#5C21A4"))
        } else {
            nextBtn.setBackgroundColor(Color.parseColor("#BBBBBB"))
        }
    }

}