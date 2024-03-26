package com.example.teuniverse

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.provider.Settings
import android.transition.Transition
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File

class SignupProfileActivity:AppCompatActivity() {
    private val PICK_IMAGE_REQUEST = 1
    private var selectedImagePath: String? = null
    private lateinit var profileImg: ImageView
    private lateinit var name: EditText
    private lateinit var nextBtn: Button
    private lateinit var textCount: TextView
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_profile)

        nextBtn = findViewById(R.id.next_btn)
        val backBtn = findViewById<ImageButton>(R.id.back_btn_detail)
        val galleryBtn = findViewById<ImageButton>(R.id.gallery_btn)
        profileImg = findViewById(R.id.profile_image)
        name = findViewById(R.id.set_name)
        textCount = findViewById(R.id.text_count)

        setInitialProfile()

        backBtn.setOnClickListener{
            UserInfoDB.init(this)
            val isExist = UserInfoDB.doesFileExist(this)
            val db = UserInfoDB.getInstance().all
            val editor = UserInfoDB.getInstance().edit()

            if(isExist && db.containsKey("edit")) {
                if (db.getValue("edit") == 1) {
                    val menuActivityIntent = Intent(this, MenuActivity::class.java)
                    // Intent에 프래그먼트로 이동할 것임을 표시
                    menuActivityIntent.putExtra("goToProfileFragment", true)
                    startActivity(menuActivityIntent)
                    editor.putInt("edit", 0)
                    editor.apply()
                } else {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            } else {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        // 갤러리에서 이미지를 선택하기 위한 버튼 클릭 이벤트 등록
        galleryBtn.setOnClickListener {
            // 외부 저장소 액세스 권한 확인 및 요청
            checkExternalStorageAccess()
        }
    }

    private fun setInitialProfile() {
        Log.d("setInitialProfile 함수","실행")
        UserInfoDB.init(this)
        val userDB = UserInfoDB.getInstance()
        // 내부 저장소에 저장된 모든 키-값 쌍 가져오기
        val allEntries: Map<String, *> = userDB.all

        // 프로필 설정
        if(userDB.contains("thumbnailUrl")) {
            val profileUrl = allEntries.getValue("thumbnailUrl")

            Glide.with(this)
                .load(profileUrl)
                .apply(RequestOptions.circleCropTransform()) // 이미지뷰 모양에 맞추기
                .into(profileImg)
        } else if(userDB.contains("imageFile")) {
            val profileUrl = allEntries.getValue("imageFile")

            Glide.with(this)
                .load(profileUrl)
                .apply(RequestOptions.circleCropTransform()) // 이미지뷰 모양에 맞추기
                .into(profileImg)
        } else {
            Glide.with(this)
                .load(R.drawable.profile)
                .apply(RequestOptions.circleCropTransform()) // 이미지뷰 모양에 맞추기
                .into(profileImg)
        }

        // 닉네임 설정
        val nickName = allEntries.getValue("nickName")
        countText(nickName)
    }

    // 갤러리에서 선택한 이미지를 처리하는 메서드
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 이미지 첨부한 경우
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            putImage(data)
        } else { // 이미지 첨부 안하는 경우

        }
    }

    // 이미지 첨부한 경우 처리
    @RequiresApi(Build.VERSION_CODES.R)
    private fun putImage(data: Intent?) {
        val selectedImageUri = data?.data // 이미지 들고옴
        selectedImagePath = getPathFromUri(selectedImageUri) // 실제 경로 가져옴
        Log.d("path", selectedImagePath.toString())

        selectedImageUri?.let { uri ->
            Glide.with(this)
                .load(uri)
                .apply(RequestOptions.circleCropTransform()) // 이미지뷰 모양에 맞추기
                .into(profileImg)

            UserInfoDB.init(this)
            val editor = UserInfoDB.getInstance().edit()
            editor.putString("imageFile", selectedImagePath.toString())
            editor.putString("thumbnailUrl", null)
            editor.apply()

        } ?: Log.e("putImage", "Selected image URI is null")
    }

    // Uri에서 실제 파일 경로 가져오기
    private fun getPathFromUri(uri: Uri?): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        contentResolver.query(uri!!, projection, null, null, null)?.use { cursor ->
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            return cursor.getString(columnIndex)
        }
        return null
    }

    // 외부 저장소 액세스 권한 확인 및 요청
    @RequiresApi(Build.VERSION_CODES.R)
    private fun checkExternalStorageAccess() {
        if (Environment.isExternalStorageManager()) {
            // 외부 저장소 액세스 권한이 부여되어 있다면 갤러리를 엽니다.
            openGallery()
        } else {
            // 외부 저장소 액세스 권한이 부여되어 있지 않다면 사용자에게 권한을 요청합니다.
            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            startActivity(intent)
        }
    }

    // 갤러리를 엽니다.
    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST)
    }

    private fun countText(nickName: Any?) {
        name.text = Editable.Factory.getInstance().newEditable(nickName.toString())
        nextBtn.setBackgroundColor(Color.parseColor("#5C21A4"))
        textCount.text = name.text.length.toString()

        nextBtn.setOnClickListener {
            nextBtnMotion()
        }

        UserInfoDB.init(this)
        val editor = UserInfoDB.getInstance().edit()

        name.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                textCount.text = "${s?.length ?: 0}"

                // 10자 제한
                if ((s?.length ?: 0) > 10) {
                    val truncatedText = s?.substring(0, 10)
                    name.setText(truncatedText)
                    name.setSelection(10)
                }

                editor.putString("nickName", name.text.toString())
                editor.apply()
                updateButtonColor(s)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }
            override fun afterTextChanged(s: Editable?) {

            }
        })
    }

    private fun updateButtonColor(text: CharSequence?) {
        if (text.isNullOrBlank()) {
            // 텍스트가 비어있을 때
            nextBtn.setBackgroundColor(Color.parseColor("#BBBBBB"))
            nextBtn.setOnClickListener {
                Toast.makeText(this, "이름을 입력하세요", Toast.LENGTH_SHORT).show()
            }
        } else {
            // 텍스트가 작성되었을 때
            nextBtn.setBackgroundColor(Color.parseColor("#5C21A4"))
            nextBtn.setOnClickListener {
                nextBtnMotion()
            }
        }
    }

    // 다음 버튼 클릭이벤트
    private fun nextBtnMotion() {
        UserInfoDB.init(this)
        val db = UserInfoDB.getInstance().all
        val editor = UserInfoDB.getInstance().edit()

        if (db.containsKey("edit")) {
            if (db.getValue("edit") == 1) {
                continueWithImageFileAccess()
                editor.putInt("edit", 0)
                editor.apply()
            } else {
                val intent = Intent(this, SignupApprovalActivity::class.java)
                startActivity(intent)
                finish()
            }
        } else {
            val intent = Intent(this, SignupApprovalActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun continueWithImageFileAccess() {
        UserInfoDB.init(this)
        val userInfo = UserInfoDB.getInstance().all
        val id = userInfo.getValue("id").toString()
        val nickName = userInfo.getValue("nickName").toString()
        val favoriteArtistId = userInfo.getValue("favoriteArtistId").toString()

        var thumbnailUrl: RequestBody? = null
        var imageFile: MultipartBody.Part? = null

        if (userInfo.containsKey("thumbnailUrl")) { // 카카오 프로필 이용하는 경우
            val url = userInfo.getValue("thumbnailUrl").toString()
            thumbnailUrl = url.toRequestBody("text/plain".toMediaTypeOrNull())
        } else if (userInfo.containsKey("imageFile")) { // 갤러리에서 선택한 경우
            val imagePath = userInfo.getValue("imageFile").toString()
            val file = File(imagePath)
            val requestFile = RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file)
            imageFile = MultipartBody.Part.createFormData("imageFile", file.name, requestFile)
        }

        val idRequestBody = id.toRequestBody("text/plain".toMediaType())
        val nickNameRequestBody = nickName.toRequestBody("text/plain".toMediaType())
        val favoriteArtistIdRequestBody = favoriteArtistId.toRequestBody("text/plain".toMediaType())

        // 코루틴을 사용하여 서버로 회원가입 정보 전송
        lifecycleScope.launch {
            signUpInfoToServer(idRequestBody, nickNameRequestBody, thumbnailUrl, favoriteArtistIdRequestBody, imageFile)
        }
    }

    private suspend fun signUpInfoToServer(id: RequestBody, nickName: RequestBody, thumbnailUrl: RequestBody?, favoriteArtistId: RequestBody, imageFile: MultipartBody.Part?) {
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
                Toast.makeText(this, "프로필 수정 실패", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            // 예외 처리 코드
            handleError(e.message ?: "Unknown error occurred.")
            Toast.makeText(this, "프로필 수정 실패", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleResponse() {
        Toast.makeText(this, "프로필 수정 성공", Toast.LENGTH_SHORT).show()
        val menuActivityIntent = Intent(this, MenuActivity::class.java)
        // Intent에 프래그먼트로 이동할 것임을 표시
        menuActivityIntent.putExtra("goToProfileFragment", true)
        startActivity(menuActivityIntent)
    }

    private fun handleError(errorMessage: String) {
        // 에러를 처리하는 코드
        Log.d("프로필 수정 api Error", errorMessage)
    }
}