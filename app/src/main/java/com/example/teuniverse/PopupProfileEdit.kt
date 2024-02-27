package com.example.teuniverse

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.teuniverse.databinding.PopupProfileEditBinding

class PopupProfileEdit(context: Context): Dialog(context) {
    private lateinit var binding: PopupProfileEditBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PopupProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        getProfileImg()
        countPostContent()

        // 창 닫기
        binding.close.setOnClickListener {
            dismiss()
        }
    }

    // 글자수 세기
    private fun countPostContent() {
        binding.setNickname.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.textCount.text = "${s?.length ?: 0}"

                // 10자 제한
                if ((s?.length ?: 0) > 10) {
                    val truncatedText = s?.substring(0, 10)
                    binding.setNickname.setText(truncatedText)
                    binding.setNickname.setSelection(10)
                }

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
            binding.doneBtn.setBackgroundResource(R.drawable.btn_round_gray)
            binding.doneBtn.setOnClickListener {
                Toast.makeText(context, "이름을 입력하세요", Toast.LENGTH_SHORT).show()
            }
        } else {
            // 텍스트가 비어있지 않을 때
            binding.doneBtn.setBackgroundResource(R.drawable.enroll_button_event)
            binding.doneBtn.setOnClickListener {
                // 특정 작업 수행
                dismiss()
            }
        }
    }

    // 유저 이미지 넣기
    private fun getProfileImg() {
        MainActivity.UserInfoDB.init(context)
        val db = MainActivity.UserInfoDB.getInstance().all
        val userImg = db.getValue("thumbnailUrl")

        Glide.with(context)
            .load(userImg)
            .apply(RequestOptions.circleCropTransform()) // 이미지뷰 모양에 맞추기
            .into(binding.userProfile)

    }

    private fun initViews() = with(binding) {
        // 뒤로가기 버튼, 빈 화면 터치를 통해 dialog가 사라지지 않도록
        setCancelable(false)
        // background를 투명하게 만듦
        // (중요) Dialog는 내부적으로 뒤에 흰 사각형 배경이 존재하므로, 배경을 투명하게 만들지 않으면
        // corner radius의 적용이 보이지 않는다.
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
}