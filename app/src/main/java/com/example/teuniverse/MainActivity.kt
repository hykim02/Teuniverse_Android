package com.example.teuniverse

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.OAuthLoginCallback
import com.navercorp.nid.oauth.view.NidOAuthLoginButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.util.Calendar


class MainActivity : AppCompatActivity() {

    lateinit var naverLogin: NidOAuthLoginButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val kakaoLogin = findViewById<ImageButton>(R.id.kakao_login)
        naverLogin = findViewById(R.id.buttonOAuthLoginImg)

        // 초기화
        NaverIdLoginSDK.initialize(this, getString(R.string.naver_client_id), getString(R.string.naver_client_secret), "Teuniverse")
        initMissionDB()

        // 최초 로그인 시 필히 실행(회원가입 테스트용)
//        ServiceAccessTokenDB.init(this)
//        val tokenEditor = ServiceAccessTokenDB.getInstance().edit()
//        UserInfoDB.init(this)
//        val userEditor = UserInfoDB.getInstance().edit()
//
//        tokenEditor.clear()
//        tokenEditor.apply()
//        userEditor.clear()
//        userEditor.apply()

//        ScheduleTypeDB.init(this)
//        val editor = ScheduleTypeDB.getInstance().edit()
//        editor.clear()
//        editor.apply()

//        kakaoUnlink()

        // 기존회원인지 여부확인하는 조건 다시 설정하기
        kakaoLogin.setOnClickListener{
            kakaoCheck()
        }

        naverLoginApi()
    }

    private fun kakaoCheck() {
        // 유저 DB 파일이 존재한다면 (회원가입 한적 있는 경우)
        if(UserInfoDB.doesFileExist(this)){
            Log.d("db확인","회원")
            // 토큰 검증 api
            lifecycleScope.launch {
                checkToken()
            }
        } else { // 회원가입 한적 없는 경우
            Log.d("db확인","비회원")
            kakaoLoginApi()
        }
    }

    // 네이버 로그인 api
    private fun naverLoginApi() {
        Log.d("naverLoginApi", "실행")
        naverLogin.setOAuthLogin(object : OAuthLoginCallback {
            override fun onSuccess() {
                // 네이버 로그인 인증이 성공했을 때 수행할 코드 추가
                val accessToken = NaverIdLoginSDK.getAccessToken()

                // 코루틴을 사용하여 pushToken 함수 호출
                lifecycleScope.launch {
                    pushToken(1, accessToken.toString())
                }
            }
            override fun onFailure(httpStatus: Int, message: String) {
                val errorCode = NaverIdLoginSDK.getLastErrorCode().code
                val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
                Log.e("naver Api error","errorCode:$errorCode, errorDesc:$errorDescription")
            }
            override fun onError(errorCode: Int, message: String) {
                onFailure(errorCode, message)
            }
        })
    }

    // 카카오 로그인 api
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
            }
        }

        // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 아니면 카카오계정으로 로그인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this) { token, error ->
                if (error != null) { // 에러 발생한 경우
                    Log.e(TAG, "카카오톡으로 로그인 실패", error)
                    // 사용자가 카카오톡 설치 후 디바이스 권한 요청 화면에서 로그인을 취소한 경우,
                    // 의도적인 로그인 취소로 보고 카카오계정으로 로그인 시도 없이 로그인 취소로 처리 (예: 뒤로 가기)
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    }
                    // 카카오톡에 연결된 카카오계정이 없는 경우, 카카오계정으로 로그인 시도
                    UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)

                } else if (token != null) { // 로그인 성공 후 토큰 발급된 경우
                    Log.i(TAG, "카카오톡으로 로그인 성공 ${token.accessToken}")

                    lifecycleScope.launch {
                        pushToken(0, token.accessToken)
                    }
                }
            }
        } else { // 카카오톡이 설치되어 있지 않은 경우
            try {
                Log.d("카카오계정 로그인 시도","콜백")
                UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
            } catch (e: Exception) {
                pushTokenError("Exception during loginWithKakaoAccount: ${e.message}", null)
            }
        }
    }

    // loginType -> 0: 카카오, 1: 네이버
    private suspend fun pushToken(loginType: Int, accessToken: String) {
        Log.d("pushToken 함수", "실행")
        try {
            // IO 스레드에서 Retrofit 호출 및 코루틴 실행
            // Retrofit을 사용해 서버에서 받아온 응답을 저장하는 변수
            // Response는 Retrofit이 제공하는 HTTP 응답 객체
            val loginRequest = LoginRequest(loginType = loginType, accessToken = accessToken)
            val response: Response<ServerResponse<LoginData>> = withContext(Dispatchers.IO) {
                LoginInstance.userLoginService().userLogin(loginRequest)
            }
            val serverResponse: ServerResponse<LoginData>? = response.body()

            // Response를 처리하는 코드
            if (response.isSuccessful) { // success가 true일 때
                Log.d("서버 연동 성공", response.toString())
                if (serverResponse != null) {
                    pushTokenResponse(serverResponse)
                } else {
                    pushTokenError("Response body is null.", null)
                }
            } else { // success가 false일 때
                Log.d("pushToken 함수 에러", "로그인 실패")
                pushTokenError("Error: ${response.code()} - ${response.message()}", serverResponse)
            }
        } catch (e: Exception) {
            // 예외 처리 코드
            pushTokenError(e.message ?: "Unknown error occurred.", null)
        }
    }

    private fun pushTokenResponse(serverResponse: ServerResponse<LoginData>?) {
        Log.d("pushTokenResponse 함수","실행")

        UserInfoDB.init(this) // 유저 db 초기화 및 생성
        val userEditor = UserInfoDB.getInstance().edit()
        val userDB = UserInfoDB.getInstance()
        ServiceAccessTokenDB.init(this) // 토큰 db 초기화 및 생성
        val tokenEditor = ServiceAccessTokenDB.getInstance().edit()

        val userData = serverResponse?.data // 서버 응답 데이터

        if (userData != null) {
            Log.d("userData", userData.toString())
            Log.d("isExistUser", userData.isExistUser.toString())

            // 이미 존재하는 회원 true (토큰 재발급하는 경우)
            if (userData.isExistUser) {
                tokenEditor.putString("accessToken", userData.accessToken)
                tokenEditor.putString("refreshToken", userData.refreshToken)

                // 홈 화면으로 이동
                val intent = Intent(this, MenuActivity::class.java)
                startActivity(intent)
                finish()
            }
            else { // 신규 회원 false (처음 회원 가입 시)
                tokenEditor.putString("accessToken", userData.accessToken)
                tokenEditor.putString("refreshToken", userData.refreshToken)
                userEditor.putLong("id", userData.userProfileData.id)
                userEditor.putString("nickName", userData.userProfileData.nickName)
                userEditor.putString("thumbnailUrl", userData.userProfileData.thumbnailUrl)

                if(userDB.contains("imageFile")) {
                    userEditor.putString("imageFile", null)
                    userEditor.apply()
                }

                // 회원 가입 진행
                val intent = Intent(this, SignupProfileActivity::class.java)
                startActivity(intent)
                finish()
            }
            userEditor.apply()
            tokenEditor.apply()
            } else {
                Log.d("userData","null")
            }
        }

    private fun pushTokenError(errorMessage: String, response: ServerResponse<LoginData>?) {
        // 에러를 처리하는 코드
        Log.d("pushTokenError", errorMessage)

        // 로그인 실패한 경우 (토큰 만료 시)
        if (response != null) {
            if(!response.success) {
                kakaoLogout() // 로그 아웃
            }
        }
    }

    // 토큰 검증 api
    private suspend fun checkToken() {
        Log.d("checkToken 함수", "실행")
        ServiceAccessTokenDB.init(this)
        val token = ServiceAccessTokenDB.getInstance().getString("accessToken", "null")
        Log.d("token",token.toString())
        val accessToken = token?.let { AccessToken(accessToken = it) }
        try {
            // IO 스레드에서 Retrofit 호출 및 코루틴 실행
            // Retrofit을 사용해 서버에서 받아온 응답을 저장하는 변수
            // Response는 Retrofit이 제공하는 HTTP 응답 객체
            if(accessToken != null) {
                val response: Response<SignUpResponse> = withContext(Dispatchers.IO) {
                    CheckTokenInstance.checkTokenService().checkToken(accessToken)
                }
                val serverResponse: SignUpResponse? = response.body()

                // Response를 처리하는 코드
                if (response.isSuccessful) {
                    Log.d("토큰 검증 api isSuccessful", response.toString())
                    checkTokenResponse(serverResponse)
                } else { // success가 false일 때
                    Log.d("checkToken 함수 에러", response.toString())
                    kakaoLoginApi()
                }
            } else {
                Log.d("accessToken","null")
            }
        } catch (e: Exception) {
            // 예외 처리 코드
            pushTokenError(e.message ?: "Unknown error occurred.", null)
        }
    }

    private fun checkTokenResponse(response: SignUpResponse?) {
        if(response?.success == true) { // 토큰 유효한 경우
            Log.d("토큰","유효함")
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
            finish()
        } else { // 토큰 만료된 경우
            Log.d("토큰","만료됨")
            kakaoLogout() // 로그아웃
            kakaoLoginApi() // 재로그인
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

    private fun kakaoLogout() {
        // 로그아웃
        UserApiClient.instance.logout { error ->
            if (error != null) {
                Log.e("Hello", "로그아웃 실패. SDK에서 토큰 삭제됨", error)
            } else {
                Log.i("Hello", "로그아웃 성공. SDK에서 토큰 삭제됨")
                Toast.makeText(this, "로그아웃 되었습니다", Toast.LENGTH_SHORT).show()
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
}

