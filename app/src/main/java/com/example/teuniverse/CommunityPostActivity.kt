package com.example.teuniverse

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.teuniverse.databinding.ActivityCommunityPostBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class CommunityPostActivity: AppCompatActivity() {

    private lateinit var binding: ActivityCommunityPostBinding
    private val PICK_IMAGE_REQUEST = 1
    private var selectedImagePath: String? = null
    private lateinit var bitmap: Bitmap
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCommunityPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 갤러리에서 이미지를 선택하기 위한 버튼 클릭 이벤트 등록
        binding.galleryBtn.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST)
        }

        binding.closeBtn.setOnClickListener {
            navigateToCommunityFragment()
        }

        countPostContent() // 글자수 세기 및 500자 제한
        applyBtn()
    }

    // 갤러리에서 선택한 이미지를 처리하는 메서드
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 이미지 첨부한 경우
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            putImage(data)
        } else { // 이미지 첨부 안하는 경우
            noneImage()
        }
    }

    // 이미지 첨부한 경우 처리
    private fun putImage(data: Intent?) {
        val selectedImageUri = data?.data // 이미지 들고옴
        Log.d("selectedImageUri", selectedImageUri.toString())
        selectedImagePath = getPathFromUri(selectedImageUri) // 실제 경로 가져옴
        Log.d("selectedImagePath", selectedImagePath.toString())
        binding.postImg.setImageURI(selectedImageUri) // 게시물 작성 페이지에 이미지 넣음

        binding.postImg.visibility = View.VISIBLE // 이미지뷰를 보이도록 설정

        // 이미지뷰에서 Drawable 얻기
        val drawable: Drawable? = binding.postImg.drawable
        Log.d("drawable", drawable.toString())
        //Drawable에서 Bitmap으로 변환
        bitmap = (drawable as BitmapDrawable).bitmap // bitmap에 이미지 저장되어 있음
        Log.d("bitmap", bitmap.toString())
    }

    // 이미지 첨부 안하는 경우 처리
    private fun noneImage() {
        // 이미지뷰를 숨기도록 설정
        binding.postImg.visibility = GONE
    }

    // 게시물 등록
    @RequiresApi(Build.VERSION_CODES.O)
    private fun applyBtn() {
        binding.applyBtn.setOnClickListener {
            val content = binding.postContent.text.toString() // 게시글 내용
            // 게시글 내용은 10글자 이상이어야 함
            if (content.length < 10) { // 10글자 미만인 경우
                Toast.makeText(this,"10글자 이상 작성 해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                // 이미지 첨부한 경우
                if (selectedImagePath != null) {
                    val imageFile = createMultipartBody(bitmap)
                    lifecycleScope.launch {
                        postToServerApi(RequestBody.create("text/plain".toMediaType(), content), imageFile)
                        voteMissionApi(10, 4) // 글쓰기 미션 10표(3회)
                    }
                } else { // 이미지 첨부 안한 경우
                    binding.postImg.visibility = GONE
                    lifecycleScope.launch {
                        postToServerApi(RequestBody.create("text/plain".toMediaType(), content), null)
                        voteMissionApi(10, 4) // 글쓰기 미션 10표(3회)
                    }
                }
                // 이후에 네비게이션 등 필요한 로직 추가
                navigateToCommunityFragment()
            }
        }
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

    // 글자수 세기
    private fun countPostContent() {
        binding.postContent.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.textCount.text = "${s?.length ?: 0}"
                // 500자 제한
                if ((s?.length ?: 0) > 500) {
                    val truncatedText = s?.substring(0, 500)
                    binding.postContent.setText(truncatedText)
                    binding.postContent.setSelection(500)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not used in this example
            }
            override fun afterTextChanged(s: Editable?) {

            }
        })
    }

    // 게시글 데이터 서버로 전송
    private suspend fun postToServerApi(content: RequestBody, imageFile: MultipartBody.Part?) {
        Log.d("postToServerApi 함수", "호출 성공")
        val accessToken = getAccessToken()
        try {
            // IO 스레드에서 Retrofit 호출 및 코루틴 실행
            // Retrofit을 사용해 서버에서 받아온 응답을 저장하는 변수
            // Response는 Retrofit이 제공하는 HTTP 응답 객체
            val response: Response<SignUpResponse> = withContext(Dispatchers.IO) {
                CommunityPostInstance.communityPostService().postContent(accessToken, content, imageFile)
            }
            // Response를 처리하는 코드
            if (response.isSuccessful) {
                val postSuccess: SignUpResponse? = response.body()
                if (postSuccess != null) {
                    Log.d("artistList", "${postSuccess.statusCode} ${postSuccess.message}")
                    handleResponse()
                } else {
                    handleError("Response body is null.")
                }
            } else {
                Toast.makeText(this, "피드 생성 실패", Toast.LENGTH_SHORT).show()
                handleError("Error: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            // 예외 처리 코드
            handleError(e.message ?: "Unknown error occurred.")
        }
    }

    // 투표권 지급 미션 api
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun voteMissionApi(voteCount: Int, type: Int) {
        Log.d("voteMissionApi", "호출 성공")
        val accessToken = getAccessToken()
        val params = VoteMission(voteCount = voteCount, type = type)
        try {
            if (accessToken != null) {
                val response: Response<ServerResponse<NumberOfVote>> = withContext(
                    Dispatchers.IO) {
                    GiveVoteInstance.giveVoteService().giveVote(accessToken, params)
                }
                if (response.isSuccessful) {
                    val theVotes: ServerResponse<NumberOfVote>? = response.body()
                    if (theVotes != null) {
                        Toast.makeText(this, "일일미션 글쓰기 완료", Toast.LENGTH_SHORT).show()
                        Log.d("피드생성 미션", "${theVotes.statusCode} ${theVotes.message}")
                    } else {
                        Toast.makeText(this, "일일미션 글쓰기 실패", Toast.LENGTH_SHORT).show()
                        handleError("Response body is null.")
                    }
                } else {
                    Toast.makeText(this, "일일미션 글쓰기 실패", Toast.LENGTH_SHORT).show()
                    handleError("피드생성 미션 Error: ${response.code()} - ${response.message()}")
                }
            }
        }
        catch (e: Exception) {
            handleError(e.message ?: "Unknown error occurred.")
        }
    }

    private fun handleResponse() {
        Toast.makeText(this, "피드 생성 성공", Toast.LENGTH_SHORT).show()
    }

    private fun handleError(errorMessage: String) {
        // 에러를 처리하는 코드
        Log.d("Api Error", errorMessage)
    }

    // close 버튼 클릭 시 호출되는 함수
    private fun navigateToCommunityFragment() {
        val menuActivityIntent = Intent(this, MenuActivity::class.java)
        // Intent에 프래그먼트로 이동할 것임을 표시
        menuActivityIntent.putExtra("goToCommunityFragment", true)
        startActivity(menuActivityIntent)
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