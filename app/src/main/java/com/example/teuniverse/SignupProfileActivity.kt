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

    //바인딩 객체 선언
    private var mBinding: ActivitySignupProfileBinding? = null
    private val binding get() = mBinding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

        galleryBtn.setOnClickListener{
            //갤러리 호출
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            activityResult.launch(intent)
        }
    }

    private val activityResult: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode == RESULT_OK && it.data != null){
            val uri = it.data!!.data

            //화면에 보여주기
            Glide.with(this).load(uri).into(profileImg)
        }
    }
}