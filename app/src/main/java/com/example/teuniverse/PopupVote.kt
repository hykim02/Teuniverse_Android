package com.example.teuniverse

import PopupVoteCheck
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.core.widget.addTextChangedListener
import com.example.teuniverse.databinding.PopupVoteBinding

// 뷰를 띄워야하므로 Dialog 클래스는 context를 인자로 받음
class PopupVote(context: Context, private val remainVote: Int):
    Dialog(context) {

    private lateinit var binding : PopupVoteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 만들어놓은 팝업창 띄움
        binding = PopupVoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()

        // editText의 변화가 감지되면
        binding.votes.addTextChangedListener {
            val votes = binding.votes.text.toString().toIntOrNull()
            Log.d("votes", votes.toString())
            Log.d("remainVote", remainVote.toString())

            if (votes != null) {
                if((remainVote - votes) <= 0) {
                    binding.remainVote.text = "0"
                } else {
                    binding.remainVote.text = (remainVote - votes).toString()
                }
            } else {
                binding.remainVote.text = "000"
            }
        }

        // 투표 버튼
        binding.bntVoting.setOnClickListener {
            val voteCount = binding.votes.text.toString().toInt() // 투표할 개수
            Log.d("voteCount", voteCount.toString())

            // 투표할 투표권 개수보다 보유투표권이 적을 겨우
            if(voteCount > remainVote) {
                val popupVoteFail = PopupVoteFail(context)
                popupVoteFail.show()
                dismiss()
            } else {
                // 객체 생성(매개변수로 데이터 전달)
                val popupVoteCheck = PopupVoteCheck(context, voteCount)
                popupVoteCheck.show()
                dismiss()
            }
        }

        // 취소 버튼
        binding.btnRevoke.setOnClickListener{
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