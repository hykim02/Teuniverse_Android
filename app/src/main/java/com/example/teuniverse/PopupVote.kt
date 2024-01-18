package com.example.teuniverse

import PopupVoteCheck
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat.startActivity
import com.example.teuniverse.VoteFragment.Companion.REQUEST_CODE_POPUP_VOTE_CHECK
import com.example.teuniverse.databinding.PopupVoteBinding
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

// 뷰를 띄워야하므로 Dialog 클래스는 context를 인자로 받음
class PopupVote(context: Context, private val okCallback: (String) -> Unit): Dialog(context) {

    private lateinit var binding : PopupVoteBinding

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 만들어놓은 팝업창 띄움
        binding = PopupVoteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()

        MainActivity.UserInfoDB.init(context)
        val keysOfDB = MainActivity.UserInfoDB.getInstance()
        var currentVote: Int? = null

        for ((key, value) in keysOfDB.all) {
            if (key == "voteCount") {
                currentVote = value as Int
                Log.d("currentVote", currentVote.toString())
            }
        }
        // 화면에 다이얼로그가 나타난 후에 투표 정보 가져오기
        GlobalScope.launch {
            // 투표 정보 가져오기
            if (currentVote != null) {
                getPopupVoteApi(currentVote)
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

        // 투표 버튼
        binding.bntVoting.setOnClickListener {
            // 객체 생성(매개변수로 데이터 전달)
            val popupVoteCheck = PopupVoteCheck(
                context,
                binding.tvVotes.text.toString(),
                binding.tvArtistName.text.toString(),
                binding.tvMonth.text.toString(),
                binding.tvPercent.text.toString(),
                okCallback
            )
            popupVoteCheck.show()
            dismiss()
        }
    }

    private suspend fun getPopupVoteApi(voteCount: Int) {
        Log.d("getPopupVoteApi 함수", "호출 성공")
        val accessToken = getAccessToken()
        val voteRequest = NumberOfVote(voteCount = voteCount)
        try {
            if (accessToken != null) {
                val response: Response<ServerResponse<PopupVoteData>> = withContext(Dispatchers.IO) {
                    PopupVoteInstance.getCurrentVoteInfoService().getCurrentVoteInfo(accessToken, voteRequest)
                }
                if (response.isSuccessful) {
                    val voteInfo: ServerResponse<PopupVoteData>? = response.body()
                    if (voteInfo != null) {
                        Log.d("getPopupVoteApi 함수", "${voteInfo.statusCode} ${voteInfo.message}")
                        handlePopupVote(voteInfo)
                    } else {
                        handleError("Response body is null.")
                    }
                } else {
                    handleError("getPopupVoteApi 함수 Error: ${response.code()} - ${response.message()}")
                }
            }
        }
        catch (e: Exception) {
            handleError(e.message ?: "Unknown error occurred.")
        }
    }

    private fun handlePopupVote(voteInfo: ServerResponse<PopupVoteData>) {
        Log.d("handlePopupVote 함수","호출 성공")
        binding.tvVotes.text = voteInfo.data.voteCount.toString() // 투표하는 개수
        binding.tvVotes2.text = voteInfo.data.voteCount.toString()
        binding.tvArtistName.text = voteInfo.data.artistName // 아티스트 이름
        binding.tvArtistName2.text = voteInfo.data.artistName
        binding.tvArtistName3.text = voteInfo.data.artistName
        binding.remainVote.text = voteInfo.data.remainVoteCount.toString() // 보유 투표권
        binding.tvMonth.text = voteInfo.data.month.toString() // 월
        binding.tvPercent.text = voteInfo.data.rank.toString() // 비율



    }

    private fun getAccessToken(): String? {
        MainActivity.ServiceAccessTokenDB.init(context)
        val serviceTokenDB = MainActivity.ServiceAccessTokenDB.getInstance()
        var accessToken: String? = null

        for ((key, value) in serviceTokenDB.all) {
            if (key == "accessToken") {
                accessToken = "Bearer " + value.toString()
            }
        }
        return accessToken
    }

    private fun handleError(errorMessage: String) {
        // 에러를 처리하는 코드
        Log.d("getPopupVoteApi 함수 Error", errorMessage)
    }
}