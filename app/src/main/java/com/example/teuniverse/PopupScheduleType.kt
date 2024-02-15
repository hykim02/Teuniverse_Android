package com.example.teuniverse

import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import com.example.teuniverse.databinding.PopupScheduleTypeBinding

class PopupScheduleType(context: Context): Dialog(context) {
    private lateinit var binding : PopupScheduleTypeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PopupScheduleTypeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
        setCheckBox()

        binding.completeBtn.setOnClickListener {
            dismiss()
        }

        binding.closeBtn.setOnClickListener {
            dismiss()
        }
    }

    private fun setCheckBox() {
        binding.videoChk.setOnClickListener {
            confirmCheckBox("video", binding.videoChk)
        }
        binding.cakeChk.setOnClickListener {
            confirmCheckBox("cake", binding.cakeChk)
        }
        binding.festivalChk.setOnClickListener {
            confirmCheckBox("festival", binding.festivalChk)
        }
        binding.moreChk.setOnClickListener {
            confirmCheckBox("more", binding.moreChk)
        }
    }

    private fun confirmCheckBox(chkbox: String, image: ImageButton) {
        ScheduleTypeDB.init(context)
        val typeDB = ScheduleTypeDB.getInstance().all
        val editor = ScheduleTypeDB.getInstance().edit()
        val src = "checkbox_$chkbox"
        val resources: Resources = context.resources
        val packageName: String = context.packageName

        if (typeDB.containsKey(chkbox)) { // 키가 있는 경우
            val value = typeDB.getValue(chkbox)
            Log.d(chkbox, value.toString())
            if (value == true) {
                editor.putBoolean(chkbox, false)
                image.setImageResource(R.drawable.checkbox)
                editor.apply()
            } else { // false 인 경우
                editor.putBoolean(chkbox, true)
                val resID = resources.getIdentifier(src, "drawable", packageName)
                image.setImageResource(resID)
                editor.apply()
            }
        } else { // 키가 없는 경우(처음 클릭)
            editor.putBoolean(chkbox, true)
            val resID = resources.getIdentifier(src, "drawable", packageName)
            image.setImageResource(resID)
            editor.apply()
        }

    }
    private fun initViews() = with(binding) {
        // 뒤로가기 버튼, 빈 화면 터치를 통해 dialog가 사라지지 않도록
        setCancelable(false)

        // background를 투명하게 만듦
        // (중요) Dialog는 내부적으로 뒤에 흰 사각형 배경이 존재하므로, 배경을 투명하게 만들지 않으면
        // corner radius의 적용이 보이지 않는다.
        window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
    }
}