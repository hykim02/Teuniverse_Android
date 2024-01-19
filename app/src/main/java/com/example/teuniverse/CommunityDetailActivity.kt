package com.example.teuniverse

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class CommunityDetailActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.community_detail)

        val backBtn = findViewById<ImageButton>(R.id.back_btn_detail)
        val userImg = findViewById<ImageView>(R.id.user_profile)
        val userName = findViewById<TextView>(R.id.fandom_name)
        val postTime = findViewById<TextView>(R.id.term)
        val postImg = findViewById<ImageView>(R.id.post_img)
        val content = findViewById<TextView>(R.id.post_summary)
        val heartCount = findViewById<TextView>(R.id.heart_count)

        // Intent로부터 데이터 받아오기
        val receivedItem = intent.getParcelableExtra<CommunityPostItem>("communityItem")

        // 받아온 데이터 활용
        if (receivedItem != null) {
            Glide.with(this)
                .load(receivedItem.userImg) // currentItem.img가 이미지 URL인 경우
                .apply(RequestOptions.circleCropTransform()) // 이미지뷰 모양에 맞추기
                .into(userImg)

            Glide.with(this)
                .load(receivedItem.postImg) // currentItem.img가 이미지 URL인 경우
                .apply(RequestOptions.circleCropTransform()) // 이미지뷰 모양에 맞추기
                .into(postImg)

            userName.text = receivedItem.fandomName
            postTime.text = receivedItem.postTerm
            content.text = receivedItem.postSummary
            heartCount.text = receivedItem.heartCount.toString()
        }

        backBtn.setOnClickListener {
            val intent = Intent(this, CommunityFragment::class.java)
            startActivity(intent)
            finish()
        }
    }
}