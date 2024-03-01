package com.example.teuniverse

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class SignupEndActivity:AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_end)

        val homeButton = findViewById<Button>(R.id.btn_home)

        MainActivity.UserInfoDB.init(this)
        val userInfo = MainActivity.UserInfoDB.getInstance()
        var id: Long? = null
        var nickName: String? = null
        var thumbnailUrl: String? = null
        var favoriteArtistId: Int? = null

        for ((key, value) in userInfo.all) {
            if (key == "id") {
                id = value as Long
            } else if (key == "nickName") {
                nickName = value.toString()
            } else if (key == "thumbnailUrl") {
                thumbnailUrl = value.toString()
            } else
                favoriteArtistId = value as Int
        }

        homeButton.setOnClickListener{
            // 코루틴을 사용하여 pushToken 함수 호출
            lifecycleScope.launch {
                if (id != null && nickName != null
                    && thumbnailUrl != null && favoriteArtistId != null) {
                    signUpInfoToServer(id, nickName, thumbnailUrl, favoriteArtistId, null)
                }
            }
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private suspend fun signUpInfoToServer(id: Long, nickName: String, thumbnailUrl: String, favoriteArtistId: Int, imageFile: String?) {
        Log.d("signUpInfoToServer 함수", "호출 성공")
        try {
            // IO 스레드에서 Retrofit 호출 및 코루틴 실행
            // Retrofit을 사용해 서버에서 받아온 응답을 저장하는 변수
            // Response는 Retrofit이 제공하는 HTTP 응답 객체
            val signupRequest = SignUpRequest(id = id, nickName = nickName, thumbnailUrl = thumbnailUrl, favoriteArtistId = favoriteArtistId, imageFile = imageFile )
            val response: Response<SignUpResponse> = withContext(Dispatchers.IO) {
                SignUpInstance.signUpSuccessService().signUpSuccess(signupRequest)
            }
            // Response를 처리하는 코드
            if (response.isSuccessful) {
                val signUpSuccess: SignUpResponse? = response.body()
                if (signUpSuccess != null) {
                    Log.d("artistList", "${signUpSuccess.statusCode} ${signUpSuccess.message}")
                    handleResponse(signUpSuccess)
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

    private fun handleResponse(signUpSuccess: SignUpResponse) {
        if (signUpSuccess != null) {
           Log.d("회원가입", signUpSuccess.toString())
        }
    }

    private fun handleError(errorMessage: String) {
        // 에러를 처리하는 코드
        Log.d("Error", errorMessage)
    }
}