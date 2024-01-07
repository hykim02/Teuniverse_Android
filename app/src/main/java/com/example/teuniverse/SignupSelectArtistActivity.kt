package com.example.teuniverse

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.gridlayout.widget.GridLayout
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response


class SignupSelectArtistActivity:AppCompatActivity() {

//    private var gridLayout: GridLayout = findViewById(R.id.artist_gridlayout)
    private lateinit var gridLayout: GridLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_select_artist)

        gridLayout = findViewById(R.id.artist_gridlayout)
        val nextBtn = findViewById<Button>(R.id.next_btn)
        val backBtn = findViewById<ImageButton>(R.id.back_btn)
        val filterSpinner = findViewById<Spinner>(R.id.select_spinner)
        val searchTxt = findViewById<EditText>(R.id.search_txt)

        searchTxt.hint = "아티스트를 검색하세요"

        backBtn.setOnClickListener {
            val intent = Intent(this, SignupApprovalActivity::class.java)
            startActivity(intent)
            finish()
        }

        var spinnerList = arrayOf("인기순", "가나다순")
        var spinnerAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, spinnerList)
        filterSpinner.adapter = spinnerAdapter
        filterSpinner.setSelection(0)

        // 코루틴을 사용하여 getArtistList 함수 호출
        lifecycleScope.launch {
            getArtistList()
        }

        // 아티스트 선택 함수
        selectedArtist()
        // 버튼 상태를 업데이트
        updateButtonState()
    }

    // 서버에서 아티스트 데이터 가져오는 함수
    private suspend fun getArtistList() {
        Log.d("getArtistList 함수", "호출 성공")
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
                    Log.d("artistList", "${artistList.statusCode} ${artistList.message}")
                    handleResponse(artistList)
                } else {
                    handleError("Response body is null.")
                }
            } else {
                Log.d("error", "서버 연동 실패")
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
        // artistList를 처리하는 코드
        if (artistList != null) {
            // 이미지뷰와 텍스트뷰의 인덱스를 반복문으로 순회
            for (i in artistList.data.indices) {
                // 이미지 데이터 추출
                val artistData = artistList.data[i]
                val imageUrl = artistData.thumbnailUrl
                val nametxt = artistData.name

                // 이미지뷰 ID
                val imageViewId = resources.getIdentifier("artist${i + 1}", "id", packageName)
                // 이미지뷰 가져오기
                val imageView = findViewById<ImageView>(imageViewId)

                // 텍스트뷰 ID
                val textViewId = resources.getIdentifier("name${i + 1}", "id", packageName)
                // 텍스트뷰 가져오기
                val textView = findViewById<TextView>(textViewId)

                // Glide를 사용하여 이미지 로딩
                Glide.with(this)
                    .load(imageUrl)
                    .apply(RequestOptions.circleCropTransform()) // 이미지뷰 모양에 맞추기
                    .into(imageView)

                // 텍스트뷰에 아티스트 데이터 연결
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


    // 클릭된 아이템 인식 함수
    private fun selectedArtist() {
        Log.d("selectedArtist 함수","실행")
        for (i in 0 until gridLayout.childCount) {
            val childView = gridLayout.getChildAt(i)

            if (childView is ImageView) {
                childView.setOnClickListener {
                    onImageViewClick(it) // 테두리 그리기
                    // 클릭된 이미지뷰가 있을 때마다 버튼 상태 업데이트
                    updateButtonState()
                }
            }
        }
    }

    // 클릭된 아이템 테두리 그리는 함수
    fun onImageViewClick(view: View) {
        Log.d("onImageViewClick 함수","실행")
        if (view is ImageView) {
            // 그리드 레이아웃에 있는 모든 자식 뷰에 대해 반복
            for (i in 0 until gridLayout.childCount) {
                val childView = gridLayout.getChildAt(i)
                if (childView is ImageView) {
                    // 클릭된 이미지뷰와 현재 순회 중인 이미지뷰가 같으면 클릭 상태를 true로, 아니면 false로 설정
                    childView.isActivated = childView == view

                    // 클릭된 이미지뷰에 테두리를 그리기
                    if (childView.isActivated) {
                        childView.setBackgroundResource(R.drawable.custom_circle_ap)
                    } else {
                        // 다른 이미지뷰들의 테두리 제거
                        childView.setBackgroundResource(0)
                    }
                }
            }
        }
    }

    // 버튼 색상 변경 함수
    private fun updateButtonState() {
        var trueCount = 0

        for (i in 0 until gridLayout.childCount) {
            val childView = gridLayout.getChildAt(i)
            if (childView is ImageView && childView.isActivated) {
                trueCount++
            }
        }

        // 클릭된 이미지뷰의 개수가 하나라면 버튼 색상 변경
        val nextBtn = findViewById<Button>(R.id.next_btn)
        if (trueCount == 1) {
            nextBtn.setBackgroundColor(Color.parseColor("#5C21A4"))
            nextBtn.setOnClickListener {
                val intent = Intent(this, SignupEndActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}