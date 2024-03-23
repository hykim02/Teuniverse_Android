package com.example.teuniverse

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.teuniverse.databinding.PopupDeleteBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class PopupDelete(context: Context, private val feedId: String,
    private val deleteListener: PopupDeleteListener): Dialog(context) {
    private lateinit var binding : PopupDeleteBinding

    interface PopupDeleteListener {
        fun deleteFeed(feedId: Int)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PopupDeleteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()

        binding.cancelBtn.setOnClickListener{
            dismiss()
        }

        binding.deleteBtn.setOnClickListener{
            // 삭제 API 호출
            GlobalScope.launch {
                deleteFeedApi(feedId)
            }
            deleteListener.deleteFeed(feedId.toInt())
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

    // 피드 삭제 api
    private suspend fun deleteFeedApi(feedId: String) {
        Log.d("deleteFeedsApi 함수", "호출 성공")
        val accessToken = getAccessToken()
        try {
            if (accessToken != null) {
                val response: Response<SignUpResponse> = withContext(
                    Dispatchers.IO) {
                    DeleteFeedInstance.deleteFeedService().deleteFeed(feedId, accessToken)
                }
                if (response.isSuccessful) {
                    val theDeleteFeed: SignUpResponse? = response.body()
                    if (theDeleteFeed != null) {

                        Log.d("deleteFeedApi 함수 response", "${theDeleteFeed.statusCode} ${theDeleteFeed.message}")
                    } else {
                        Toast.makeText(context, "피드 삭제 실패", Toast.LENGTH_SHORT).show()
                        handleError("Response body is null.")
                    }
                } else {
                    Toast.makeText(context, "피드 삭제 실패", Toast.LENGTH_SHORT).show()
                    handleError("deleteFeedApi 함수 Error: ${response.code()} - ${response.message()}")
                }
            }
        }
        catch (e: Exception) {
            handleError(e.message ?: "Unknown error occurred.")
        }
    }

    private fun handleError(errorMessage: String) {
        // 에러를 처리하는 코드
        Log.d("delete Api 함수 Error", errorMessage)
    }

    // db에서 토큰 가져오기
    private fun getAccessToken(): String? {
        ServiceAccessTokenDB.init(context)
        val serviceTokenDB = ServiceAccessTokenDB.getInstance()
        var accessToken: String? = null

        for ((key, value) in serviceTokenDB.all) {
            if (key == "accessToken") {
                accessToken = "Bearer " + value.toString()
            }
        }
        return accessToken
    }
}