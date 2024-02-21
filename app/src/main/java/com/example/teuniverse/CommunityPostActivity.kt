package com.example.teuniverse

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.View.GONE
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
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
//            val manager: FragmentManager = supportFragmentManager
//            val transaction: FragmentTransaction = manager.beginTransaction()
//            transaction.replace(R.id.post_activity, CommunityFragment).commit()
            val intent = Intent(this, CommunityFragment::class.java)
            startActivity(intent)
            finish()
        }

        countPostContent() // 글자수 세기 및 500자 제한
//        applyBtn()
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
        val selectedImageUri = data?.data
        selectedImagePath = getPathFromUri(selectedImageUri)
        binding.postImg.setImageURI(selectedImageUri)

        // 이미지뷰를 보이도록 설정
        binding.postImg.visibility = View.VISIBLE

        // 이미지뷰에서 Drawable 얻기
        val drawable: Drawable? = binding.postImg.drawable
        // Drawable에서 Bitmap으로 변환
        bitmap = (drawable as BitmapDrawable).bitmap // bitmap에 이미지 저장되어 있음
        val imageFile = createMultipartBody(bitmap) // MultipartBody 생성 함수

        setContents(imageFile)
    }

    // 이미지 첨부 안하는 경우 처리
    private fun noneImage() {
        // 이미지뷰를 숨기도록 설정
        binding.postImg.visibility = GONE

        setContents(null)
    }

    private fun setContents(imgFile: MultipartBody.Part?) {
        val content = binding.postContent.text.toString() // 게시글 내용
        val contentBody = RequestBody.create("text/plain".toMediaType(), content)

        binding.applyBtn.setOnClickListener {
            // 서버로 데이터 전송
            lifecycleScope.launch {
                postToServerApi(contentBody, imgFile)
            }
            navigateToCommunityFragment()
            finish()
        }
    }


    // 게시물 등록
//    private fun applyBtn() {
//        binding.applyBtn.setOnClickListener {
//            Log.d("applyBtn", "Clicked!")
//            val content = binding.postContent.text.toString() // 게시글 내용
//
//            // 이미지 첨부한 경우
//            if (selectedImagePath != null) {
//                val imageFile = createMultipartBody(bitmap)
//                lifecycleScope.launch {
//                    postToServerApi(RequestBody.create("text/plain".toMediaType(), content), imageFile)
//                }
//            } else { // 이미지 첨부 안한 경우
//                binding.postImg.visibility = GONE
//                val emptyImageFile = createEmptyImageFile()
//                val imageFilePart = createMultipartBodyFile(emptyImageFile)
//
//                lifecycleScope.launch {
//                    postToServerApi(RequestBody.create("text/plain".toMediaType(), content), imageFilePart)
//                }
//            }
//            // 이후에 네비게이션 등 필요한 로직 추가
//            navigateToCommunityFragment()
//            finish()
//        }
//    }


    // 빈 이미지 파일 생성
    private fun createEmptyImageFile(): File {
        val emptyBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        return bitmapToFile(emptyBitmap)
    }

    private fun createMultipartBodyFile(file: File): MultipartBody.Part {
        // 이미지 파일을 RequestBody로 변환
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        // MultipartBody.Part 생성
        return MultipartBody.Part.createFormData("imageFile", file.name, requestFile)
    }

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
                // Not used in this example
            }
        })
    }

    // Uri에서 실제 파일 경로 가져오기
    private fun getPathFromUri(uri: Uri?): String {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(uri!!, projection, null, null, null)
        val columnIndex = cursor!!.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        val path = cursor.getString(columnIndex)
        cursor.close()
        return path
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
                    handleResponse(postSuccess)
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

    private fun handleResponse(postSuccess: SignUpResponse) {
        if (postSuccess != null) {
            Log.d("게시물 작성", postSuccess.toString())
        }
    }

    private fun handleError(errorMessage: String) {
        // 에러를 처리하는 코드
        Log.d("Error", errorMessage)
    }

    // close 버튼 클릭 시 호출되는 함수
    private fun navigateToCommunityFragment() {
        // MenuActivity로 이동하여 CommunityFragment로 이동하는 코드
        val menuIntent = Intent(this, MenuActivity::class.java)
        menuIntent.putExtra("destinationFragment", R.id.navigation_community)
        startActivity(menuIntent)
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