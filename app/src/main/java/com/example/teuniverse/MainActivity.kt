package com.example.teuniverse

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.lifecycle.lifecycleScope
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response


class MainActivity : AppCompatActivity() {
    // 서버에서 받은 서비스 access 토큰 저장
    object ServiceAccessTokenDB {
        private lateinit var sharedPreferences: SharedPreferences
        // 초기화
        fun init(context: Context) {
            sharedPreferences = context.getSharedPreferences("ServiceToken", Context.MODE_PRIVATE)
        }

        // 객체 반환
        fun getInstance(): SharedPreferences {
            if(!this::sharedPreferences.isInitialized) {
                throw IllegalStateException("SharedPreferencesSingleton is not initialized")
            }
            return sharedPreferences
        }
    }
    // 로그인 후 서버에서 유저ID 받아와서 저장
    object UserInfoDB {
        private lateinit var sharedPreferences: SharedPreferences
        // 초기화
        fun init(context: Context) {
            sharedPreferences = context.getSharedPreferences("UserInfo", Context.MODE_PRIVATE)
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
        setContentView(R.layout.activity_main)

        val kakaoLogin = findViewById<ImageButton>(R.id.kakao_login)
        val naverLogin = findViewById<ImageButton>(R.id.naver_login)

//        kakaoUnlink()

        kakaoLogin.setOnClickListener{
            // 카카오계정으로 로그인 공통 callback 구성
            // 카카오톡으로 로그인 할 수 없어 카카오계정으로 로그인할 경우 사용됨
            val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
                if (error != null) {
                    Log.e(TAG, "카카오계정으로 로그인 실패", error)
                } else if (token != null) {
                    Log.i(TAG, "카카오계정으로 로그인 성공 ${token.accessToken}")
                    // 코루틴을 사용하여 pushToken 함수 호출
                    lifecycleScope.launch {
                        pushToken(0, token.accessToken)
                    }
                    // 화면 전환
                    val intent = Intent(this, SignupProfileActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }

            // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
                UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                    if (error != null) {
                        Log.e(TAG, "카카오톡으로 로그인 실패", error)
                        // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
                        // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
                        if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                            return@loginWithKakaoTalk
                        }

                        // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                        UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
                    } else if (token != null) {
                        Log.i(TAG, "카카오톡으로 로그인 성공 ${token.accessToken}")
                        // 코루틴을 사용하여 getArtistList 함수 호출
                        lifecycleScope.launch {
                            pushToken(0, token.accessToken)
                        }
                        // 화면 전환
                        val intent = Intent(this, SignupProfileActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            } else {
                try {
                    Log.d("카카오계정 로그인","콜백")
                    UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
                } catch (e: Exception) {
                    Log.e(TAG, "Exception during loginWithKakaoAccount", e)
                    handleError("Exception during loginWithKakaoAccount: ${e.message}")
                }
            }

            // 사용자 정보 요청 (기본)
            UserApiClient.instance.me { user, error ->
                if (error != null) {
                    Log.e(TAG, "사용자 정보 요청 실패", error)
                }
                else if (user != null) {
                    Log.i(TAG, "사용자 정보 요청 성공" +
                            "\n회원번호: ${user.id}" +
                            "\n이메일: ${user.kakaoAccount?.email}" +
                            "\n닉네임: ${user.kakaoAccount?.profile?.nickname}" +
                            "\n프로필사진: ${user.kakaoAccount?.profile?.thumbnailImageUrl}")
                }
            }
        }

        naverLogin.setOnClickListener{
            val intent = Intent(this, SignupSelectArtistActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // loginType -> 0: 카카오, 1: 네이버
    private suspend fun pushToken(loginType: Int, accessToken: String) {
        Log.d("pushToken 함수", "호출 성공")

        try {
            // IO 스레드에서 Retrofit 호출 및 코루틴 실행
            // Retrofit을 사용해 서버에서 받아온 응답을 저장하는 변수
            // Response는 Retrofit이 제공하는 HTTP 응답 객체
            val loginRequest = LoginRequest(loginType = loginType, accessToken = accessToken)
            val response: Response<ServerResponse<LoginData>> = withContext(Dispatchers.IO) {
                LoginInstance.userLoginService().userLogin(loginRequest)
            }

            // Response를 처리하는 코드
            if (response.isSuccessful) {
                Log.d("서버 연동",response.message() + response.code())

                val serverResponse: ServerResponse<LoginData>? = response.body()
                if (serverResponse != null) {
                    Log.d("serverResponse", serverResponse.toString())
                    handleResponse(serverResponse)

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

    // 객체 생성 (중첩 클래스)
//    class OkHttpLoginInterface(private val client: OkHttpClient) {
//        private val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
//
//        fun userLogin(request: LoginRequest): LoginResponse {
//            val baseUrl = BASE_URL
//            val endpoint = "user/login"
//            val url = "$baseUrl$endpoint"
//
//            val json = """
//            {
//                "loginType": ${request.loginType},
//                "accessToken": "${request.accessToken}"
//            }
//        """.trimIndent()
//
//            val requestBody = json.toRequestBody(mediaType)
//
//            val requestToServer = Request.Builder()
//                .url(url)
//                .post(requestBody)
//                .addHeader("accept", "application/json")
//                .addHeader("Content-Type", "application/json")
//                .build()
//
//            return try {
//                val response = client.newCall(requestToServer).execute()
//
//                if (response.isSuccessful) {
//                    val responseBody = response.body?.string()
//                    if (responseBody != null) {
//                        Log.d("responseBody",responseBody)
//                    }
//                    return Gson().fromJson(responseBody, LoginResponse::class.java)
//                } else {
//                    throw IOException("Error: ${response.code} - ${response.message}")
//                }
//            } catch (e: IOException) {
//                throw IOException("Network error: ${e.message}", e)
//            }
//        }
//    }


//    private suspend fun pushToken(loginType: Int, accessToken: String) {
//        Log.d("pushToken 함수", "호출 성공")
//
//        try {
//            val loginRequest = LoginRequest(loginType = loginType, accessToken = accessToken)
//            val response: LoginResponse = withContext(Dispatchers.IO) {
//                OkHttpLoginInstance.userLoginService().userLogin(loginRequest)
//            }
//
//            Log.d("서버 연동", "성공")
//            Log.d("serverResponse", response.toString())
//            handleResponse(response)
//
//        } catch (e: HttpException) {
//            handleError("Error: ${e.code()} - ${e.message()}")
//        } catch (e: IOException) {
//            handleError("Network error: ${e.message}")
//        } catch (e: Exception) {
//            handleError("Unknown error occurred.")
//        }
//    }

    private fun handleResponse(userData: ServerResponse<LoginData>?) {
        Log.d("handleResponse 함수","실행")
        //SharedPreferences 초기화
        UserInfoDB.init(this)
        val userEditor = UserInfoDB.getInstance().edit()
        ServiceAccessTokenDB.init(this)
        val tokenEditor = ServiceAccessTokenDB.getInstance().edit()

        if (userData != null) {
            Log.d("userData","null 아님")
            // 이미 존재하는 회원 true
            if (userData.data) {
                Log.d("isExistUser", userData.data.isExistUser.toString())
                Log.d("userData",userData.toString())
                tokenEditor.putString("accessToken", userData.data.accessToken)
                tokenEditor.putString("refreshToken", userData.data.refreshToken)
            }
            // 신규 회원 false
            else {
                Log.d("isExistUser",userData.data.isExistUser.toString())
                Log.d("userData",userData.toString())
                tokenEditor.putString("accessToken", userData.data.accessToken)
                tokenEditor.putString("refreshToken", userData.data.refreshToken)
                userEditor.putLong("id", userData.data.userProfileData.id)
                userEditor.putString("nickName", userData.data.userProfileData.nickName)
                userEditor.putString("thumbnailUrl", userData.data.userProfileData.thumbnailUrl)
            }
            userEditor.apply()
            tokenEditor.apply()
        } else {
            handleError("userData is null.")
        }
    }

    // 연결 끊기
    private fun kakaoUnlink() {
        UserApiClient.instance.unlink { error ->
            if (error != null) {
                Log.e("Hello", "연결 끊기 실패", error)
            } else {
                Log.i("Hello", "연결 끊기 성공. SDK에서 토큰 삭제 됨")
            }
        }
        finish()
    }

    private fun handleError(errorMessage: String) {
        // 에러를 처리하는 코드
        Log.d("Error", errorMessage)
    }
}