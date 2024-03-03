package com.example.teuniverse

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SignupApprovalActivity:AppCompatActivity() {

    private lateinit var nextBtn: Button
    private lateinit var backBtn: ImageButton
    private lateinit var chk1: CheckBox
    private lateinit var chk2: CheckBox
    private lateinit var chk4: CheckBox
    private lateinit var detail: TextView
    private lateinit var detail2: TextView

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_approval)

        nextBtn = findViewById(R.id.next_btn)
        backBtn = findViewById(R.id.back_btn_detail)
        chk1 = findViewById(R.id.checkBox)
        chk2 = findViewById(R.id.checkBox2)
        chk4 = findViewById(R.id.checkBox4)
        detail = findViewById(R.id.detail)
        detail2 = findViewById(R.id.detail2)

        setCheckBoxListeners()

        detail.setOnClickListener {
            val uri = "https://axiomatic-bottle-f3c.notion.site/5652ff1803d24772b8516eb2a95c324e"
            var intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            startActivity(intent)
        }

        detail2.setOnClickListener {
            val uri = "https://axiomatic-bottle-f3c.notion.site/42127cf9966e49d38b50a8190d969852"
            var intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            startActivity(intent)
        }
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