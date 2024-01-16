package com.example.teuniverse

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class VoteFragment : Fragment() {
    private lateinit var fanTab: TextView
    private lateinit var fanAll: TextView
    private lateinit var fanBest: TextView
    private lateinit var artistTab: TextView
    private lateinit var rvRanking: RecyclerView
    private lateinit var rankingList: ArrayList<VoteRankingItem>
    private lateinit var voteRankAdapter: VoteRankAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 코루틴을 사용하여 getArtistList 함수 호출
        lifecycleScope.launch {
            getRankingOfArtists()
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_vote, container, false)
        // 아이템 선언
        fanTab = view.findViewById(R.id.fan_tab)
        artistTab = view.findViewById(R.id.artist_tab)
        fanAll = view.findViewById(R.id.fan_tab_all)
        fanBest = view.findViewById(R.id.fan_tab_best)
        rvRanking = view.findViewById(R.id.rv_ranking)
        rankingList = ArrayList()

        voteRankAdapter = VoteRankAdapter(rankingList)

        // 리사이클러뷰 어댑터 연결
        rvRanking.adapter = voteRankAdapter
        rvRanking.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        // 뷰 숨기기
        fanAll.visibility = View.GONE
        fanBest.visibility = View.GONE

        // 팬 탭 클릭이벤트
        fanTab.setOnClickListener {
            // 기본 값: 전체
            lifecycleScope.launch {
                getRankingOfFan(0)
            }
            // 뷰 나타내기
            fanAll.visibility = View.VISIBLE
            fanBest.visibility = View.VISIBLE
            // 색상 변경
            fanTab.setTextColor(Color.parseColor("#5C21A4"))
            artistTab.setTextColor(Color.parseColor("#7C7C7C"))
            // 팬: 전체
            fanAll.setOnClickListener {
                lifecycleScope.launch {
                    getRankingOfFan(0)
                }
                fanAll.setTextColor(Color.parseColor("#5C21A4"))
                fanBest.setTextColor(Color.parseColor("#7C7C7C"))
            }
            // 팬: 최애
            fanBest.setOnClickListener {
                lifecycleScope.launch {
                    getRankingOfFan(1)
                }
                fanAll.setTextColor(Color.parseColor("#7C7C7C"))
                fanBest.setTextColor(Color.parseColor("#5C21A4"))
            }

            // 아티스트 탭 클릭이벤트
            artistTab.setOnClickListener {
                lifecycleScope.launch {
                    getRankingOfArtists()
                }
                // 뷰 숨기기
                fanAll.visibility = View.GONE
                fanBest.visibility = View.GONE
                // 색상 변경
                fanTab.setTextColor(Color.parseColor("#7C7C7C"))
                artistTab.setTextColor(Color.parseColor("#5C21A4"))
            }
        }
        return view
    }

    // 서버에서 월간 아티스트 투표 데이터 가져오는 함수
    private suspend fun getRankingOfArtists() {
        Log.d("getRankingOfArtists 함수", "호출 성공")
        try {
            // IO 스레드에서 Retrofit 호출 및 코루틴 실행
            // Retrofit을 사용해 서버에서 받아온 응답을 저장하는 변수
            // Response는 Retrofit이 제공하는 HTTP 응답 객체
            val serviceToken = getString(R.string.serviceToken)
            val response: Response<ServerResponse<VoteData>> = withContext(Dispatchers.IO) {
                MonthlyArtistRankingInstance.getVoteCountService().getVoteCount(serviceToken)
            }
            // Response를 처리하는 코드
            if (response.isSuccessful) {
                val artistVoteCountList: ServerResponse<VoteData>? = response.body()
                if (artistVoteCountList != null) {
                    Log.d("voteCountList", "${artistVoteCountList.statusCode} ${artistVoteCountList.message}")
                    // 정렬 기준에 따른 순서
                    handleResponse(artistVoteCountList)
                    rankingData(artistVoteCountList)
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

    // 팬 순위 가져오기
    private suspend fun getRankingOfFan(type: Int) {
        Log.d("getRankingOfFan 함수", "호출 성공")
        try {
            Log.d("try문","실행")
            // IO 스레드에서 Retrofit 호출 및 코루틴 실행
            // Retrofit을 사용해 서버에서 받아온 응답을 저장하는 변수
            // Response는 Retrofit이 제공하는 HTTP 응답 객체
            val serviceToken = getString(R.string.serviceToken)
            val response: Response<ServerResponse<VoteData>> = withContext(Dispatchers.IO) {
                MonthlyFanRankingInstance.getVoteCountService().getVoteCount(serviceToken, type)
            }
            // Response를 처리하는 코드
            if (response.isSuccessful) {
                Log.d("팬랭킹 response","응답 성공")
                val fanVoteCountList: ServerResponse<VoteData>? = response.body()
                if (fanVoteCountList != null) {
                    Log.d("voteCountList", "${fanVoteCountList.statusCode} ${fanVoteCountList.message}")
                    // 팬 순위 클릭 시 데이터 조회 (기본: 전체)
                    rankingData(fanVoteCountList)
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

    // 1~4위 까지
    @SuppressLint("DiscouragedApi")
    private fun handleResponse(voteCountList: ServerResponse<VoteData>?) {
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

    // 월간 아티스트 & 팬 순위
    @SuppressLint("DiscouragedApi", "SetTextI18n", "NotifyDataSetChanged")
    private fun rankingData(voteCountList: ServerResponse<VoteData>?) {
        rankingList.clear()
        if (voteCountList != null) {
            // 이미지뷰와 텍스트뷰의 인덱스 반복문으로 순회
            for (i in voteCountList.data.indices) {
                val voteCountData = voteCountList.data[i]
                val imageUrl = voteCountData.thumbnailUrl
                val nametxt = voteCountData.name
                var numberOfVotes = voteCountData.voteCount

                Log.d(nametxt,voteCountData.toString())

                if (i in 0 until 3) {
                    Log.d("if문 $i",voteCountData.toString())
                    // 이미지뷰 가져오기
                    val imageViewId = resources.getIdentifier("raking_${i+1}th_img", "id", requireContext().packageName)
                    val imageView = view?.findViewById<ImageView>(imageViewId)

                    // name 텍스트뷰 가져오기
                    val nameTvId = resources.getIdentifier("ranking_${i+1}th_name", "id", requireContext().packageName)
                    val nameTv = view?.findViewById<TextView>(nameTvId)
                    // Count 텍스트뷰 가져오기
                    val countTvId = resources.getIdentifier("ranking_${i+1}th_count","id",requireContext().packageName)
                    val countTv = view?.findViewById<TextView>(countTvId)

                    // 텍스트뷰에 데이터 연결
                    if (nameTv != null) {
                        nameTv.text = nametxt
                    }
                    if (countTv != null) {
                        numberOfVotes = addCommasToNumber(numberOfVotes)
                        countTv.text = numberOfVotes
                    }

                    // Glide를 사용하여 이미지 로딩
                    if (imageView != null) {
                        Glide.with(this)
                            .load(imageUrl)
                            .apply(RequestOptions.circleCropTransform()) // 이미지뷰 모양에 맞추기
                            .into(imageView)
                    }
                }
                else {
                    Log.d("else문 $i",voteCountData.toString())

                    numberOfVotes = addCommasToNumber(numberOfVotes)
                    rankingList.add(VoteRankingItem("${i+1}위",imageUrl,nametxt,numberOfVotes))
                }
            }
            // 어댑터에 데이터가 변경되었음을 알리기
            voteRankAdapter.notifyDataSetChanged()
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