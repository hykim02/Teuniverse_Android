package com.example.teuniverse

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class SignupProfileActivity:AppCompatActivity() {
    private val PICK_IMAGE_REQUEST = 1
    private var selectedImagePath: String? = null
    private lateinit var bitmap: Bitmap
    private lateinit var profileImg: ImageView
    private lateinit var name: EditText
    private lateinit var nextBtn: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.signup_profile)

        nextBtn = findViewById(R.id.next_btn)
        val backBtn = findViewById<ImageButton>(R.id.back_btn_detail)
        val galleryBtn = findViewById<ImageButton>(R.id.gallery_btn)
        profileImg = findViewById(R.id.profile_image)
        name = findViewById(R.id.set_name)

        setInitialProfile()

        backBtn.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // 갤러리에서 이미지를 선택하기 위한 버튼 클릭 이벤트 등록
        galleryBtn.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST)
        }
    }

    private fun setInitialProfile() {
        Log.d("setInitialProfile 함수","실행")
        MainActivity.UserInfoDB.init(this)
        val userDB = MainActivity.UserInfoDB.getInstance()
        // 내부 저장소에 저장된 모든 키-값 쌍 가져오기
        val allEntries: Map<String, *> = userDB.all

        val profileUrl = allEntries.getValue("thumbnailUrl")
        Log.d("profileUrl", profileUrl.toString())
        Glide.with(this)
            .load(profileUrl)
            .apply(RequestOptions.circleCropTransform()) // 이미지뷰 모양에 맞추기
            .into(profileImg)

        val nickName = allEntries.getValue("nickName")
        Log.d("nickName", nickName.toString())
        name.text = Editable.Factory.getInstance().newEditable(nickName.toString())

        if (name.text != null) {
            nextBtn.setBackgroundColor(Color.parseColor("#5C21A4"))
            nextBtn.setOnClickListener {
                val intent = Intent(this, SignupApprovalActivity::class.java)
                startActivity(intent)
                finish()
            }
        } else {
            nextBtn.setBackgroundColor(Color.parseColor("#BBBBBB"))
            nextBtn.setOnClickListener {
                Toast.makeText(this,"이름을 입력하세요", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 갤러리에서 선택한 이미지를 처리하는 메서드
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // 이미지 첨부한 경우
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            putImage(data)
        } else { // 이미지 첨부 안하는 경우

        }
    }

    // 이미지 첨부한 경우 처리
    private fun putImage(data: Intent?) {
        val selectedImageUri = data?.data // 이미지 들고옴
        selectedImagePath = getPathFromUri(selectedImageUri) // 실제 경로 가져옴
        Glide.with(this)
            .load(selectedImageUri)
            .apply(RequestOptions.circleCropTransform()) // 이미지뷰 모양에 맞추기
            .into(profileImg)

        MainActivity.UserInfoDB.init(this)
        val editor = MainActivity.UserInfoDB.getInstance().edit()
        editor.putString("imageFile", selectedImageUri.toString())
        editor.putString("thumbnailUrl", null)
        editor.apply()
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
}