package com.example.teuniverse

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import com.example.teuniverse.databinding.PopupMissionBinding
import com.example.teuniverse.databinding.PopupVoteBinding

class PopupVoteMission(context: Context, private val okCallback: (String) -> Unit): Dialog(context) {
    private lateinit var binding : PopupMissionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PopupMissionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()

        binding.okBtn.setOnClickListener {
            dismiss()
        }
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