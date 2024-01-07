package com.example.teuniverse

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response


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

        // 코루틴을 사용하여 getArtistList 함수 호출
        lifecycleScope.launch {
            getArtistList()
        }
    }

    // 서버에서 아티스트 데이터 가져오는 함수
    private suspend fun getArtistList() {
        Log.d("getArtistList 함수","호출 성공")
        try {
            // IO 스레드에서 Retrofit 호출 및 코루틴 실행
            // Retrofit을 사용해 서버에서 받아온 응답을 저장하는 변수
            // Response는 Retrofit이 제공하는 HTTP 응답 객체
            val response: Response<SelectArtistResponse> = withContext(Dispatchers.IO) {
                SelectArtistInstance.getArtistService().getArtist()
            }

            // Response를 처리하는 코드
            if (response.isSuccessful) {
                val artistList: SelectArtistResponse? = response.body()
                if (artistList != null) {
                    Log.d("artistList","${artistList.statusCode} ${artistList.message}")
                    handleResponse(artistList)
                } else {
                    handleError("Response body is null.")
                }
            } else {
                Log.d("error","서버 연동 실패")
                handleError("Error: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            // 예외 처리 코드
            handleError(e.message ?: "Unknown error occurred.")
        }
    }

    // 받아온 아티스트 목록을 반복문을 통해 출력
    @SuppressLint("DiscouragedApi")
    private fun handleResponse(artistList: SelectArtistResponse?) {
        // artistList 처리 코드
        if (artistList != null) {
            Log.d("아티스트 목록:", "${artistList.data}")

            // 이미지뷰에 이미지 설정
            for (i in artistList.data.indices) {
                val artistData = artistList.data[i]
                val imageUrl = artistData.thumbnailUrl
                val nametxt = artistData.name

                // 이미지뷰 ID
                val imageViewId = resources.getIdentifier("artist${i + 1}", "id", packageName)
                Log.d("imageId", imageViewId.toString())
                // 이미지뷰 가져오기
                val imageView = findViewById<ImageView>(imageViewId)

                // Glide를 사용하여 이미지 로딩
                Glide.with(this)
                    .load(imageUrl)
                    .into(imageView)

                // 텍스트뷰 ID
                val textViewId = resources.getIdentifier("textView${i + 1}", "id", packageName)
                // 텍스트뷰 가져오기
                val textView = findViewById<TextView>(textViewId)

                // 아티스트 데이터를 텍스트뷰에 설정
                textView.text = nametxt

                // 이미지뷰에 아티스트 데이터 연결
                imageView.contentDescription = nametxt
            }
        }
    }

    private fun handleError(errorMessage: String) {
        // 에러를 처리하는 코드
        Log.d("Error", errorMessage)
    }
}