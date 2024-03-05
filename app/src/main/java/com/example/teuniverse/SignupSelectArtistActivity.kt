package com.example.teuniverse

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.gridlayout.widget.GridLayout
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response


class SignupSelectArtistActivity:AppCompatActivity() {

    private lateinit var gridLayout: GridLayout
    private lateinit var filterSpinner: Spinner
    private lateinit var searchTxt: EditText
    private lateinit var artistList: ArtistServerResponse<ArtistData>
    private lateinit var finalArtistImg: ImageView
    private lateinit var finalArtistName: TextView
    private lateinit var starImg: ImageView
    private lateinit var removetxt: TextView
    private lateinit var parentView: ConstraintLayout

    object SelectArtistDB {
        private lateinit var sharedPreferences: SharedPreferences
        // 초기화
        fun init(context: Context) {
            sharedPreferences = context.getSharedPreferences("SelectArtistData", Context.MODE_PRIVATE)
        }

        // 객체 반환
        fun getInstance(): SharedPreferences {
            if(!this::sharedPreferences.isInitialized) {
                throw IllegalStateException("SharedPreferencesSingleton is not initialized")
            }
            return sharedPreferences
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_select_artist)

        gridLayout = findViewById(R.id.artist_gridlayout)
        val backBtn = findViewById<ImageButton>(R.id.back_btn_detail)
        filterSpinner = findViewById(R.id.select_spinner)
        searchTxt = findViewById(R.id.search_txt)
        finalArtistImg = findViewById(R.id.selected_artist)
        finalArtistName = findViewById(R.id.select_tv3)
        starImg = findViewById(R.id.select_img_star)
        removetxt = findViewById(R.id.select_tv4)
        parentView = findViewById(R.id.select_constLayout)

        searchTxt.hint = "아티스트를 검색하세요"

        // 검색 기능 구현
        searchTxt.addTextChangedListener ( object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(s: Editable?) {
                SelectArtistDB.init(this@SignupSelectArtistActivity)
                val db = SelectArtistDB.getInstance().all
                val searchText = s.toString()

                // 모든 자식 요소에 대해 반복
                for (i in 0 until gridLayout.childCount) {
                    val childView = gridLayout.getChildAt(i)
                    // 각 자식 요소의 visibility를 GONE으로 설정
                    childView.visibility = View.GONE
                }

                if (searchText.isBlank()) {
                    // 검색 텍스트가 비어 있을 때, 모든 레이아웃 보이도록 설정
                    for (i in artistList.data.indices) {
                        val imageViewId = resources.getIdentifier("artist${i + 1}", "id", packageName)
                        val imageView = findViewById<ImageView>(imageViewId)

                        val textViewId = resources.getIdentifier("name${i + 1}", "id", packageName)
                        val textView = findViewById<TextView>(textViewId)

                        imageView.visibility = View.VISIBLE
                        textView.visibility = View.VISIBLE
                    }
                } else {
                    for (i in artistList.data.indices) {
                        // 일치하는 아티스트인 경우 해당 이미지뷰와 텍스트뷰 표시, 그렇지 않으면 숨김
                        val imageViewId = resources.getIdentifier("artist11", "id", packageName)
                        val imageView = findViewById<ImageView>(imageViewId)

                        val textViewId = resources.getIdentifier("name11", "id", packageName)
                        val textView = findViewById<TextView>(textViewId)

                        val artistName = db.getValue("artist${i+1}.name")
                        val artistImg = db.getValue("artist${i+1}.imageUrl")

                        if (artistName != null && artistName == searchText) {
                            // 일치하는 경우
                            Glide.with(this@SignupSelectArtistActivity) // 'this' 대신 'this@YourActivity' 사용
                                .load(artistImg)
                                .apply(RequestOptions.circleCropTransform())
                                .into(imageView)
                            textView.text = artistName.toString()
                            imageView.visibility = View.VISIBLE
                            textView.visibility = View.VISIBLE
                            break
                        }

                        imageView.setOnClickListener {
                            val clickedText = textView.text
                            val clickedImg = imageView.drawable

                            Glide.with(this@SignupSelectArtistActivity)
                                .load(clickedImg)
                                .apply(RequestOptions.circleCropTransform()) // 이미지뷰 모양에 맞추기
                                .into(finalArtistImg)

                            finalArtistName.text = clickedText

                            // 특정 텍스트 뷰 삭제
                            parentView.removeView(starImg)
                            parentView.removeView(removetxt)

                            // 텍스트 글씨의 색상 변경
                            finalArtistName.setTextColor(Color.parseColor("#5C21A4"))
                            // 텍스트 글씨의 크기 변경
                            val newSize = 28f
                            finalArtistName.setTextSize(TypedValue.COMPLEX_UNIT_SP, newSize)
                        }
                    }
                }
            }
        })

        backBtn.setOnClickListener {
            val intent = Intent(this, SignupApprovalActivity::class.java)
            startActivity(intent)
            finish()
        }

        // 코루틴을 사용하여 getArtistList 함수 호출
        lifecycleScope.launch {
            getArtistList()
        }

        val spinnerList = listOf("인기순", "가나다순")
        val spinnerAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, spinnerList)
        filterSpinner.adapter = spinnerAdapter
        filterSpinner.setSelection(0)

        // 아티스트 선택 함수
        selectedArtist()
        // 버튼 상태를 업데이트
        updateButtonState()
    }

    // 서버에서 아티스트 데이터 가져오는 함수
    private suspend fun getArtistList() {
        Log.d("getArtistList 함수", "호출 성공")
        val accessToken = getAccessToken()
        try {
            // IO 스레드에서 Retrofit 호출 및 코루틴 실행
            // Retrofit을 사용해 서버에서 받아온 응답을 저장하는 변수
            // Response는 Retrofit이 제공하는 HTTP 응답 객체
            if (accessToken != null) {
                val response: Response<ArtistServerResponse<ArtistData>> = withContext(Dispatchers.IO) {
                    SelectArtistInstance.getArtistService().getArtist(accessToken)
                }
                // Response를 처리하는 코드
                if (response.isSuccessful) {
                    artistList = response.body()!!
                    if (artistList != null) {
                        Log.d("artistList", "${artistList.statusCode} ${artistList.message}")
                        // 정렬 기준에 따른 순서
                        handleResponse(artistList)
                        // 스피너 정렬 이벤트
                        filterSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                when(position){
                                    0 -> handleResponse(artistList) // 인기순(좋아요순) 정렬
                                    1 -> sortedResponse(artistList) // 가나다순 정렬
                                }
                            }
                            override fun onNothingSelected(parent: AdapterView<*>?) {
                                handleResponse(artistList) // 인기순(좋아요순) 정렬
                            }
                        }
                    } else {
                        handleError("Response body is null.")
                    }
                } else {
                    Log.d("error", "서버 연동 실패")
                    handleError("Error: ${response.code()} - ${response.message()}")
                }
            } else {
                Log.d("accessToken", "null")
            }
        } catch (e: Exception) {
            // 예외 처리 코드
            handleError(e.message ?: "Unknown error occurred.")
        }
    }

    // 받아온 아티스트 목록을 반복문을 통해 출력
    @SuppressLint("DiscouragedApi")
    private fun handleResponse(artistList: ArtistServerResponse<ArtistData>?) {
        Log.d("handleResponse","실행")
        Log.d("artistList",artistList.toString())
        //SharedPreferences 초기화
        SelectArtistDB.init(this)
        val editor = SelectArtistDB.getInstance().edit()

        // 모든 자식 요소에 대해 반복
        for (i in 0 until gridLayout.childCount) {
            val childView = gridLayout.getChildAt(i)
            // 각 자식 요소의 visibility를 GONE으로 설정
            childView.visibility = View.GONE
        }

        // artistList를 처리 코드
        if (artistList != null) {
            // 이미지뷰와 텍스트뷰의 인덱스 반복문으로 순회
            for (i in artistList.data.indices) {
                // 이미지 데이터 추출
                val artistData = artistList.data[i]
                val imageUrl = artistData.thumbnailUrl
                val nametxt = artistData.name
                val id = artistData.id

                // 각 아티스트에 대한 고유한 키 생성
                val artistKey = "artist$id"
                // 내부 저장소에 데이터 저장
                editor.putInt("$artistKey.id", id)
                editor.putString("$artistKey.imageUrl", imageUrl)
                editor.putString("$artistKey.name", nametxt)

                // 이미지뷰 가져오기
                val imageViewId = resources.getIdentifier("artist${i + 1}", "id", packageName)
                val imageView = findViewById<ImageView>(imageViewId)

                // 텍스트뷰 가져오기
                val textViewId = resources.getIdentifier("name${i + 1}", "id", packageName)
                val textView = findViewById<TextView>(textViewId)

                imageView.visibility = View.VISIBLE
                textView.visibility = View.VISIBLE

                // Glide를 사용하여 이미지 로딩
                Glide.with(this)
                    .load(imageUrl)
                    .apply(RequestOptions.circleCropTransform()) // 이미지뷰 모양에 맞추기
                    .into(imageView)

                textView.text = nametxt // 텍스트뷰에 아티스트 데이터 연결
                imageView.contentDescription = nametxt // 이미지뷰에 아티스트 데이터 연결
            }
            editor.apply()
        } else {
            Log.d("artistList","null")
        }
    }

    // 가나다순 정렬 함수
    @SuppressLint("DiscouragedApi")
    private fun sortedResponse(artistList: ArtistServerResponse<ArtistData>?) {
        val unsortedList: MutableList<String> = mutableListOf()
        var sortedList: MutableList<String>

        // 리스트에 이름 데이터만 추가
        if(artistList != null) {
            for (i in artistList.data.indices) {
                val artistData = artistList.data[i]
                val artistName = artistData.name
                unsortedList.add(artistName)
            }
            Log.d("unsortedList",unsortedList.toString())
            // 가나다순으로 정렬
            sortedList = unsortedList.sorted().toMutableList()
            Log.d("sortedList",sortedList.toString())

            for (i in sortedList.indices) {
                for (j in artistList.data.indices) {
                    val artistData = artistList.data[j]
                    if(artistData.name == sortedList[i]) {
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
                        break
                    } else {
                        continue
                    }
                }
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
                    finalSelected(childView)
                    // 클릭된 이미지뷰가 있을 때마다 버튼 상태 업데이트
                    updateButtonState()
                }
            }
        }
    }

    private fun finalSelected(childView: ImageView) {
        //SharedPreferences 초기화
        SelectArtistDB.init(this)
        val artistDB = SelectArtistDB.getInstance()
        MainActivity.UserInfoDB.init(this)
        val userEditor = MainActivity.UserInfoDB.getInstance().edit()
        // 선택된 아티스트 이름 저장한 변수
        val clickedName = childView.contentDescription?.toString()
        Log.d("clickedName", clickedName.toString())
        // 내부 저장소에 저장된 모든 키-값 쌍 가져오기
        val allEntries: Map<String, *> = artistDB.all
        var foundKey: String? = null

        // 원하는 값과 일치하는 키 찾기
        for ((key, value) in allEntries) {
            // 해당 키 찾은 경우
            if (value == clickedName) {
                foundKey = key // ex) artist2.name
                break
            }
        }
        // 원하는 값과 일치하는 키를 찾았을 때의 처리
        if (foundKey != null) {
            //artist1.name에서 artist1만 추출
            val extractedKey = foundKey.substringBefore(".name")
            val resultKey = "$extractedKey.imageUrl"
            val resultID = "$extractedKey.id"

            // resultKey에 해당하는 value 찾기
            val finalImgUrl = artistDB.getString(resultKey, "")
            // 최애 아티스트 ID
            val finalArtistID = artistDB.getInt(resultID, 0)
            Log.d("finalArtistID", finalArtistID.toString())

            // DB에 저장
            userEditor.putInt("favoriteArtistId", finalArtistID)
            userEditor.apply()

            Glide.with(this)
                .load(finalImgUrl)
                .apply(RequestOptions.circleCropTransform()) // 이미지뷰 모양에 맞추기
                .into(finalArtistImg)

            finalArtistName.text = clickedName

            // 특정 텍스트 뷰 삭제
            parentView.removeView(starImg)
            parentView.removeView(removetxt)

            // 텍스트 글씨의 색상 변경
            finalArtistName.setTextColor(Color.parseColor("#5C21A4"))
            // 텍스트 글씨의 크기 변경
            val newSize = 28f
            finalArtistName.setTextSize(TypedValue.COMPLEX_UNIT_SP, newSize)

            // 원하는 값과 일치하는 키를 찾지 못했을 때의 처리
        } else {
            Log.d("failed","일치하는 키를 찾지 못함")
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

    // db에서 토큰 가져오기
    private fun getAccessToken(): String? {
        MainActivity.ServiceAccessTokenDB.init(this)
        val serviceTokenDB = MainActivity.ServiceAccessTokenDB.getInstance()
        var accessToken: String? = null

        for ((key, value) in serviceTokenDB.all) {
            if (key == "accessToken") {
                accessToken = "Bearer " + value.toString()
            }
        }
        return accessToken
    }
}