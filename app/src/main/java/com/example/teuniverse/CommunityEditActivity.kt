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
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.teuniverse.databinding.CommunityEditItemBinding
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

class CommunityEditActivity : AppCompatActivity() {
    private lateinit var binding: CommunityEditItemBinding
    private val PICK_IMAGE_REQUEST = 1
    private var selectedImagePath: String? = null
    private lateinit var bitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CommunityEditItemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.closeBtn.setOnClickListener {
            val intent = Intent(this, CommunityFragment::class.java)
            startActivity(intent)
            finish()
        }

        // 갤러리에서 이미지를 선택하기 위한 버튼 클릭 이벤트 등록
        binding.galleryBtn.setOnClickListener {
            val galleryIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST)
        }

        editEvent()
        countPostContent() // 글자수 세기
    }

    // 게시물 수정 시 넘겨 받은 데이터 처리
    private fun editEvent() {
        Log.d("editEvent함수", "실행")
        val bundle = intent.extras
        if (bundle != null) {
            val postImg = bundle.getString("postImg")
            val postSummary = bundle.getString("postSummary")
            // 이미지 로딩
            Glide.with(this)
                .load(postImg)
                .apply(RequestOptions.circleCropTransform()) // 이미지뷰 모양에 맞추기
                .into(binding.postImg)
            // 게시글 내용 넣기
            binding.postContent.setText(postSummary)
            Log.d("bundle;postImg", postImg.toString())
            Log.d("bundle;postSummary", postSummary.toString())
        } else {
            Log.e("CommunityPostActivity", "Bundle is null")
        }
        editBtn()
    }

    private fun editBtn() {
        // editBtn 클릭 리스너 등록
        binding.editBtn.setOnClickListener {
            Log.d("editBtn", "Clicked!")
            val content = binding.postContent.text.toString() // 게시글 내용
            val bundle = intent.extras
            val feedId = bundle?.getInt("feedId")

            // 이미지 첨부한 경우
            if (selectedImagePath != null) {
                val imageFile = createMultipartBody(bitmap)
                lifecycleScope.launch {
                    if (feedId != null) {
                        editFeedApi(RequestBody.create("text/plain".toMediaType(), content), imageFile, feedId)
                    }
                }
//            } else if (binding.postImg.drawable != null) { // 이미지 첨부 안한 경우, 이미지뷰에 이미지가 있는지 확인
//                val imageUri = getImageUriFromDrawable(binding.postImg.drawable)
//                val imageFilePart = createMultipartBodyFile(imageUri)
//
//                lifecycleScope.launch {
//                    if (feedId != null) {
//                        if (imageFilePart != null) {
//                            editFeedApi(RequestBody.create("text/plain".toMediaType(), content), imageFilePart, feedId)
//                        }
//                    }
//                }
            } else { // 이미지 첨부 안한 경우
                val emptyImageFile = createEmptyImageFile()
                val imageFilePart = createMultipartBodyFile(emptyImageFile)

                lifecycleScope.launch {
                    if (feedId != null) {
                        editFeedApi(RequestBody.create("text/plain".toMediaType(), content), imageFilePart, feedId)
                    }
                }
            }

            // 이후에 네비게이션 등 필요한 로직 추가
            navigateToCommunityFragment()
            finish()
        }
    }

    // Drawable에서 이미지 URI 얻기
    private fun getImageUriFromDrawable(drawable: Drawable): Uri {
        val bitmap = (drawable as BitmapDrawable).bitmap
        val imagesFolder = File(cacheDir, "images")
        imagesFolder.mkdirs()
        val file = File(imagesFolder, "temp_image.png")

        val os: OutputStream
        try {
            os = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
            os.flush()
            os.close()
        } catch (e: Exception) {
            Log.e("getImageUriFromDrawable", "Error writing bitmap to file: $e")
        }

        return Uri.fromFile(file)
    }

    // 게시글 수정 API 호출
    private suspend fun editFeedApi(content: RequestBody, imageFile: MultipartBody.Part, feedId: Int) {
        Log.d("editFeedApi 함수", "호출 성공")
        val accessToken = getAccessToken()
        try {
            // IO 스레드에서 Retrofit 호출 및 코루틴 실행
            val response: Response<SignUpResponse> = withContext(Dispatchers.IO) {
                EditFeedInstance.editFeedService().editFeed(feedId, accessToken, content, imageFile)
            }

            // Response를 처리하는 코드
            if (response.isSuccessful) {
                val editFeedSuccess: SignUpResponse? = response.body()
                if (editFeedSuccess != null) {
                    Log.d("게시물 수정", "${editFeedSuccess.statusCode} ${editFeedSuccess.message}")
                    handleResponse(editFeedSuccess)
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

    // 성공적인 응답 처리
    private fun handleResponse(postSuccess: SignUpResponse) {
        if (postSuccess != null) {
            Log.d("게시물 수정", postSuccess.toString())
        }
    }

    // 에러 응답 처리
    private fun handleError(errorMessage: String) {
        Log.d("Error", errorMessage)
    }

    // 빈 이미지 파일 생성
    private fun createEmptyImageFile(): File {
        val emptyBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        return bitmapToFile(emptyBitmap)
    }

    // 이미지 파일을 MultipartBody.Part로 변환
    private fun createMultipartBodyFile(file: File): MultipartBody.Part {
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("imageFile", file.name, requestFile)
    }

    private fun createMultipartBodyFile(uri: Uri): MultipartBody.Part {
        val file = File(getPathFromUri(uri)) // Convert Uri to File
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("imageFile", file.name, requestFile)
    }

//    private fun createMultipartBodyFile(uri: Uri?): MultipartBody.Part? {
//        uri?.let {
//            val filePath = getPathFromUri(it)
//            val file = File(filePath)
//            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
//            return MultipartBody.Part.createFormData("imageFile", file.name, requestFile)
//        }
//        return null
//    }


    // Bitmap을 File로 변환
    private fun createMultipartBody(bitmap: Bitmap): MultipartBody.Part {
        val file = bitmapToFile(bitmap)
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("imageFile", file.name, requestFile)
    }

    // Bitmap을 File로 변환
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
        binding.postContent.addTextChangedListener(object : TextWatcher {
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
                // 현재 글자수 세서 넣기
                binding.textCount.text = "${s?.length ?: 0}"
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
