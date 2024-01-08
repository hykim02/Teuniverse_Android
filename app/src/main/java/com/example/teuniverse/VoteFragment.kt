package com.example.teuniverse

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [VoteFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class VoteFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        // 코루틴을 사용하여 getArtistList 함수 호출
        lifecycleScope.launch {
            getVoteCountList()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_vote, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment VoteFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            VoteFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    // 서버에서 월간 아티스트 투표 데이터 가져오는 함수
    private suspend fun getVoteCountList() {
        Log.d("getVoteCountList 함수", "호출 성공")
        try {
            // IO 스레드에서 Retrofit 호출 및 코루틴 실행
            // Retrofit을 사용해 서버에서 받아온 응답을 저장하는 변수
            // Response는 Retrofit이 제공하는 HTTP 응답 객체
            val response: Response<MonthlyRankingResponse> = withContext(Dispatchers.IO) {
                MonthlyVoteInstance.getVoteCountService().getVoteCount()
            }
            // Response를 처리하는 코드
            if (response.isSuccessful) {
                val voteCountList: MonthlyRankingResponse? = response.body()
                if (voteCountList != null) {
                    Log.d("voteCountList", "${voteCountList.statusCode} ${voteCountList.message}")
                    // 정렬 기준에 따른 순서
                    handleResponse(voteCountList)
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

    @SuppressLint("DiscouragedApi")
    private fun handleResponse(voteCountList: MonthlyRankingResponse?) {
        if (voteCountList != null) {
            // 이미지뷰와 텍스트뷰의 인덱스 반복문으로 순회
            for (i in 0 until 4) {
                val voteCountData = voteCountList.data[i]
                val imageUrl = voteCountData.thumbnailUrl
                val nametxt = voteCountData.name
                var numberOfVotes = voteCountData.voteCount

                // 이미지뷰 가져오기
                val imageViewId = resources.getIdentifier("first_img", "id", requireContext().packageName)
                val imageView = view?.findViewById<ImageView>(imageViewId)

                // name 텍스트뷰 가져오기
                val nameTvId = resources.getIdentifier("name_${i + 1}", "id", requireContext().packageName)
                val nameTv = view?.findViewById<TextView>(nameTvId)
                // Count 텍스트뷰 가져오기
                val countTvId = resources.getIdentifier("vote_count_${i+1}","id",requireContext().packageName)
                val countTv = view?.findViewById<TextView>(countTvId)

                // 텍스트뷰에 데이터 연결
                if (nameTv != null) {
                    nameTv.text = nametxt
                }
                if (countTv != null) {
                    numberOfVotes = addCommasToNumber(numberOfVotes)
                    countTv.text = numberOfVotes
                }

                // Glide를 사용하여 이미지 로딩 (1위만)
                if (i == 0 && imageView != null) {
                    Glide.with(this)
                        .load(imageUrl)
                        .apply(RequestOptions.circleCropTransform()) // 이미지뷰 모양에 맞추기
                        .into(imageView)
                } else {
                    continue
                }
            }
        }
    }

    private fun handleError(errorMessage: String) {
        // 에러를 처리하는 코드
        Log.d("Error", errorMessage)
    }

    // 숫자 천 단위마다 ',' 넣는 함수
    private fun addCommasToNumber(number: String): String {
        val numberString = number.toString()
        val formattedNumber = StringBuilder()

        var count = 0
        for (i in numberString.length - 1 downTo 0) {
            formattedNumber.insert(0, numberString[i])
            count++
            if (count % 3 == 0 && i != 0) {
                formattedNumber.insert(0, ",")
            }
        }
        return formattedNumber.toString()
    }

}