package com.example.teuniverse

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.constraintlayout.core.motion.utils.Utils
import androidx.lifecycle.lifecycleScope
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.util.Calendar


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

        initMissionDB()

        // 최초 로그인 시 필히 실행
//        ServiceAccessTokenDB.init(this)
//        val tokenEditor = ServiceAccessTokenDB.getInstance().edit()
//        UserInfoDB.init(this)
//        val userEditor = UserInfoDB.getInstance().edit()
//
//        tokenEditor.clear()
//        tokenEditor.apply()
//        userEditor.clear()
//        userEditor.apply()
//
//        kakaoLogout()

//        ScheduleTypeDB.init(this)
//        val editor = ScheduleTypeDB.getInstance().edit()
//        editor.clear()
//        editor.apply()

//        kakaoUnlink()

        // 기존회원인지 여부확인하는 조건 다시 설정하기
        kakaoLogin.setOnClickListener{
            kakaoCheck()
        }

        naverLogin.setOnClickListener{
            val intent = Intent(this, SignupProfileActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun kakaoCheck() {
        ServiceAccessTokenDB.init(this)
        val serviceToken = ServiceAccessTokenDB.getInstance()
        UserInfoDB.init(this)
        val userData = UserInfoDB.getInstance()

        if (serviceToken.contains("accessToken") && userData.contains("id")) {
            Log.d("db확인","회원")
            // 화면 전환
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Log.d("db확인","비회원")
            kakaoLoginApi()
        }
    }

    // 투표권 미션 DB 초기화
    private fun initMissionDB() {
        Log.d("initMissionDB", "실행")
        val isExist = VoteMissionDB.doesFileExist(this)
        VoteMissionDB.init(this)
        val editor = VoteMissionDB.getInstance().edit()
        val db = VoteMissionDB.getInstance()
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_YEAR)
        Log.d("today", today.toString())

        if(isExist) {
            val day = db.getInt("today", today-1)
            if(day != today) { // 날짜가 바뀌었다면 초기화
                Log.d("미션 db","초기화 완료")
                editor.putInt("vote", 0)
                editor.putInt("attend", 0)
                editor.putInt("calendar", 0)
                editor.putInt("comment", 0)
                editor.putInt("feed", 0)
                editor.putInt("today", today)
                editor.apply()
            } else {
                Log.d("미션 참석 여부","존재함")
            }
        } else {
            Log.d("미션 db","파일 없음 초기화 진행")
            editor.putInt("vote", 0)
            editor.putInt("attend", 0)
            editor.putInt("calendar", 0)
            editor.putInt("comment", 0)
            editor.putInt("feed", 0)
            editor.putInt("today", today)
            editor.apply()
        }
    }

    private fun kakaoLoginApi() {
        Log.d("kakaoLoginApi", "실행")
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
                handleError("Exception during loginWithKakaoAccount: ${e.message}")
            }
        }
    }

    // loginType -> 0: 카카오, 1: 네이버
    private suspend fun pushToken(loginType: Int, accessToken: String) {
        Log.d("pushToken 함수", "실행")
        runBlocking {
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
    }

    private fun handleResponse(serverResponse: ServerResponse<LoginData>?) {
        Log.d("handleResponse 함수","실행")
        //SharedPreferences 초기화
        UserInfoDB.init(this)
        val userEditor = UserInfoDB.getInstance().edit()
        ServiceAccessTokenDB.init(this)
        val tokenEditor = ServiceAccessTokenDB.getInstance().edit()
        val userData = serverResponse?.data

        if (userData != null) {
            Log.d("userData", userData.toString())
            // 이미 존재하는 회원 true
            if (userData.isExistUser) {
                Log.d("isExistUser", userData.isExistUser.toString())
                tokenEditor.putString("accessToken", userData.accessToken)
                tokenEditor.putString("refreshToken", userData.refreshToken)
                // 신규 회원 false
            } else {
                Log.d("isExistUser",userData.isExistUser.toString())
                tokenEditor.putString("accessToken", userData.accessToken)
                tokenEditor.putString("refreshToken", userData.refreshToken)
                userEditor.putLong("id", userData.userProfileData.id)
                userEditor.putString("nickName", userData.userProfileData.nickName)
                userEditor.putString("thumbnailUrl", userData.userProfileData.thumbnailUrl)
            }
            userEditor.apply()
            tokenEditor.apply()
            } else {
                handleError("userData is null.")
            }
        }
    }
    private fun kakaoLogout() {
        // 로그아웃
        UserApiClient.instance.logout { error ->
            if (error != null) {
                Log.e("Hello", "로그아웃 실패. SDK에서 토큰 삭제됨", error)
            } else {
                Log.i("Hello", "로그아웃 성공. SDK에서 토큰 삭제됨")
            }
        }
    }

    // 연결 끊기
    private fun kakaoUnlink() {
        Log.d("kakaoUnlink","실행")
        UserApiClient.instance.unlink { error ->
            if (error != null) {
                Log.e("Hello", "연결 끊기 실패", error)
            } else {
                Log.i("Hello", "연결 끊기 성공. SDK에서 토큰 삭제 됨")

            }
        }
    }

    private fun handleError(errorMessage: String) {
        // 에러를 처리하는 코드
        Log.d("Error", errorMessage)
    }
