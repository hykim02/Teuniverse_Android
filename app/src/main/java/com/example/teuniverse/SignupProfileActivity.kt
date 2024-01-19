package com.example.teuniverse

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class SignupProfileActivity:AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_profile)

        val nextBtn = findViewById<Button>(R.id.next_btn)
        val backBtn = findViewById<ImageButton>(R.id.back_btn)
        val galleryBtn = findViewById<ImageButton>(R.id.gallery_btn)
        val profileImg = findViewById<ImageView>(R.id.profile_image)
        val name = findViewById<EditText>(R.id.set_name)

        MainActivity.UserInfoDB.init(this)
        val userDB = MainActivity.UserInfoDB.getInstance()
        // 내부 저장소에 저장된 모든 키-값 쌍 가져오기
        val allEntries: Map<String, *> = userDB.all

        // 닉네임 & 프로필 설정
        for ((key, value) in allEntries) {
            // 해당 키 찾은 경우
            if (key == "nickName") {
                //Log.d("nickName",value.toString())
                name.setText(value.toString())
            } else if (key == "thumbnailUrl") {
                //Log.d("profileImg",value.toString())
                Glide.with(this)
                    .load(value)
                    .apply(RequestOptions.circleCropTransform()) // 이미지뷰 모양에 맞추기
                    .into(profileImg)
            }
        }

        if (name != null) {
            nextBtn.setBackgroundColor(Color.parseColor("#5C21A4"))
            nextBtn.setOnClickListener {
                val intent = Intent(this, SignupApprovalActivity::class.java)
                startActivity(intent)
                finish()
            }
        } else {
            nextBtn.setBackgroundColor(Color.parseColor("#BBBBBB"))
            nextBtn.setOnClickListener {
                Toast.makeText(this,"이름을 입력하세요", Toast.LENGTH_SHORT).show()
            }
        }

        backBtn.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


}