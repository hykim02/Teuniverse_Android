package com.example.teuniverse

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
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
    private val PICK_IMAGE_REQUEST = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_profile)

        val nextBtn = findViewById<Button>(R.id.next_btn)
        val backBtn = findViewById<ImageButton>(R.id.back_btn_detail)
        val galleryBtn = findViewById<ImageButton>(R.id.gallery_btn)
        val profileImg = findViewById<ImageView>(R.id.profile_image)
        val name = findViewById<EditText>(R.id.set_name)

        Log.d("userDB","초기화 전")
        MainActivity.UserInfoDB.init(this)
        Log.d("userDB","초기화 후")
        val userDB = MainActivity.UserInfoDB.getInstance()
        // 내부 저장소에 저장된 모든 키-값 쌍 가져오기
        val allEntries: Map<String, *> = userDB.all

        // 닉네임 & 프로필 설정
        for ((key, value) in allEntries) {
            Log.d("for문", "실행")
            // 해당 키 찾은 경우
            if (key == "nickName") {
                Log.d("nickName",value.toString())
//                name.setText(value.toString())
                name.text = Editable.Factory.getInstance().newEditable(value.toString())
            } else if (key == "thumbnailUrl") {
                Log.d("profileImg",value.toString())
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

        // 갤러리에서 이미지를 선택하기 위한 버튼 클릭 이벤트 등록
//        galleryBtn.setOnClickListener {
//            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//            startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST)
//        }
    }


}