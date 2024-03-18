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
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget

class SignupProfileActivity:AppCompatActivity() {
    private val PICK_IMAGE_REQUEST = 1
    private var selectedImagePath: String? = null
    private lateinit var profileImg: ImageView
    private lateinit var name: EditText
    private lateinit var nextBtn: Button
    private lateinit var textCount: TextView
    private lateinit var bitmap: Bitmap
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
        UserInfoDB.init(this)
        val userDB = UserInfoDB.getInstance()
        // 내부 저장소에 저장된 모든 키-값 쌍 가져오기
        val allEntries: Map<String, *> = userDB.all

        // 프로필 설정
        val profileUrl = allEntries.getValue("thumbnailUrl")
        if (profileUrl != null) {
            Glide.with(this)
                .load(profileUrl)
                .apply(RequestOptions.circleCropTransform()) // 이미지뷰 모양에 맞추기
                .into(profileImg)
        } else {
            Log.d("thumnailUrl","존재 안함")
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

            checkExternalStorageAccess()
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
            // 파일에 접근하는 코드
        } else {
            // 사용자에게 외부 저장소 액세스 권한을 요청합니다.
            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            startActivity(intent)
        }
    }

    private fun countText(nickName: Any?) {
        name.text = Editable.Factory.getInstance().newEditable(nickName.toString())
        nextBtn.setBackgroundColor(Color.parseColor("#5C21A4"))
        textCount.text = name.text.length.toString()

        nextBtn.setOnClickListener {
            val intent = Intent(this, SignupApprovalActivity::class.java)
            startActivity(intent)
            finish()
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
                val intent = Intent(this, SignupApprovalActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}