package com.example.teuniverse

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class SignupEndActivity:AppCompatActivity() {
    private val YOUR_PERMISSION_REQUEST_CODE = 123
    private lateinit var homeButton: Button
    private lateinit var bitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_end)

        homeButton = findViewById(R.id.btn_home)

        continueWithImageFileAccess()
    }

    private fun continueWithImageFileAccess() {
        UserInfoDB.init(this)
        val userInfo = UserInfoDB.getInstance().all
        val id = userInfo.getValue("id").toString()
        val nickName = userInfo.getValue("nickName").toString()
        val favoriteArtistId = userInfo.getValue("favoriteArtistId").toString()

        var thumbnailUrl: String
        var imageFile: MultipartBody.Part? = null
        if (userInfo.containsKey("thumbnailUrl")) {
            thumbnailUrl = userInfo.getValue("thumbnailUrl").toString()
        } else {
            bitmap = userInfo.getValue("imageFile") as Bitmap
            Log.d("imgFilePath", bitmap.toString())
            // 파일 경로를 통해 Bitmap으로 변환
//            bitmap = BitmapFactory.decodeFile(imgFilePath)
//            Log.d("bitmap", bitmap.toString())
            imageFile = createMultipartBody(bitmap)
            thumbnailUrl = null.toString()
        }

        val idRequestBody = id.toRequestBody("text/plain".toMediaType())
        val nickNameRequestBody = nickName.toRequestBody("text/plain".toMediaType())
        val favoriteArtistIdRequestBody = favoriteArtistId.toRequestBody("text/plain".toMediaType())
        val thumbnailUrlRequestBody = thumbnailUrl.toRequestBody("text/plain".toMediaTypeOrNull())

        homeButton.setOnClickListener{
            // 코루틴을 사용하여 pushToken 함수 호출
            lifecycleScope.launch {
                signUpInfoToServer(idRequestBody, nickNameRequestBody, thumbnailUrlRequestBody, favoriteArtistIdRequestBody, imageFile)
            }
        }
    }

    // 이미지 파일 타입 설정
    private fun createMultipartBody(bitmap: Bitmap): MultipartBody.Part {
        val file = bitmapToFile(bitmap) // Bitmap을 File로 변환하는 함수
        // 이미지 파일을 RequestBody로 변환
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        // MultipartBody.Part 생성
        return MultipartBody.Part.createFormData("imageFile", file.name, requestFile)
    }

    // Bitmap을 File로 변환하는 함수
    private fun bitmapToFile(bitmap: Bitmap): File {
        val filesDir = applicationContext.filesDir
        val imageFile = File(filesDir, "imageFile.jpg")

        val os: OutputStream
        try {
            os = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os)
            os.flush()
            os.close()
        } catch (e: Exception) {
            Log.e("bitmapToFile", "Error writing bitmap to file: $e")
        }
        return imageFile
    }

    private suspend fun signUpInfoToServer(id: RequestBody, nickName: RequestBody, thumbnailUrl: RequestBody, favoriteArtistId: RequestBody, imageFile: MultipartBody.Part?) {
        Log.d("signUpInfoToServer 함수", "호출 성공")
        try {
            // IO 스레드에서 Retrofit 호출 및 코루틴 실행
            // Retrofit을 사용해 서버에서 받아온 응답을 저장하는 변수
            // Response는 Retrofit이 제공하는 HTTP 응답 객체
            val response: Response<SignUpResponse> = withContext(Dispatchers.IO) {
                SignUpInstance.signUpSuccessService().signUpSuccess(id, nickName, favoriteArtistId, thumbnailUrl, imageFile)
            }
            // Response를 처리하는 코드
            if (response.isSuccessful) {
                val signUpSuccess: SignUpResponse? = response.body()
                if (signUpSuccess != null) {
                    Log.d("artistList", "${signUpSuccess.statusCode} ${signUpSuccess.message}")
                    handleResponse()
                } else {
                    handleError("Response body is null.")
                }
            } else {
                Log.d("error", "서버 연동 실패")
                handleError("Error: ${response.code()} - ${response.message()}")
                Toast.makeText(this, "회원가입 실패", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            // 예외 처리 코드
            handleError(e.message ?: "Unknown error occurred.")
            Toast.makeText(this, "회원가입 실패", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleResponse() {
        Toast.makeText(this, "회원가입 성공", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, MenuActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun handleError(errorMessage: String) {
        // 에러를 처리하는 코드
        Log.d("Error", errorMessage)
    }
}