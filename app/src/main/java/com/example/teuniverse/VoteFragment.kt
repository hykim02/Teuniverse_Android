package com.example.teuniverse

import PopupVoteCheck
import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Calendar

class VoteFragment : Fragment() {
    private lateinit var fanTab: TextView
    private lateinit var fanAll: TextView
    private lateinit var fanBest: TextView
    private lateinit var artistTab: TextView
    private lateinit var rvRanking: RecyclerView
    private lateinit var rankingList: ArrayList<VoteRankingItem>
    private lateinit var voteRankAdapter: VoteRankAdapter
    private lateinit var numberOfVote: TextView
    private lateinit var voteMission: ImageView
    private lateinit var profileBtn: ImageButton
    private lateinit var voteMonth: TextView
    private lateinit var voteRemainDay: TextView
    private lateinit var voteCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 코루틴을 사용하여 getArtistList 함수 호출
        lifecycleScope.launch {
            getRankingOfArtists()
            getNumberOfVotes()
            getVoteSummary()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_vote, container, false)
        val votingBtn = view.findViewById<TextView>(R.id.vote_btn)
        // 아이템 선언
        fanTab = view.findViewById(R.id.fan_tab)
        artistTab = view.findViewById(R.id.artist_tab)
        fanAll = view.findViewById(R.id.fan_tab_all)
        fanBest = view.findViewById(R.id.fan_tab_best)
        rvRanking = view.findViewById(R.id.rv_ranking)
        rankingList = ArrayList()
        numberOfVote = view.findViewById(R.id.vote_count)
        voteMission = view.findViewById(R.id.img_btn_vote)
        voteMonth = view.findViewById(R.id.vote_tv_month)
        voteRemainDay = view.findViewById(R.id.vote_tv_lastday)
        voteCount = view.findViewById(R.id.vote_count)

        voteRankAdapter = VoteRankAdapter(rankingList)

        profileBtn = view.findViewById(R.id.img_btn_person)
        profileBtn.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_vote_to_profileFragment)
        }

        // 뷰 숨기기
        fanAll.visibility = View.GONE
        fanBest.visibility = View.GONE

        // 팬 탭 클릭이벤트
        fanTab.setOnClickListener {
            // 기본 값: 전체
            lifecycleScope.launch {
                getRankingOfFan(0)
            }
            fanTabSetting() // 클릭 시 동작 구현 함수
        }

        // 투표하기 버튼 클릭 이벤트
        votingBtn.setOnClickListener {
            val count = numberOfVote.text.toString()

            if(count.toInt() >= 1) {
                showPopupVoteDialog()
            } else {
                Toast.makeText(context, "보유한 투표권이 없습니다", Toast.LENGTH_SHORT).show()
            }
        }

        voteMission.setOnClickListener {
            // 미션창 다이얼로그 띄우기
            showPopupMissionDialog()
        }

        settingDate()

        return view
    }

    private fun visibleOfView() {
        for(i in 0 until 3) {
            // 이미지뷰 가져오기
            val imageViewId = resources.getIdentifier("raking_${i+1}th_img", "id", requireContext().packageName)
            val imageView = view?.findViewById<ImageView>(imageViewId)

            // name 텍스트뷰 가져오기
            val nameTvId = resources.getIdentifier("ranking_${i+1}th_name", "id", requireContext().packageName)
            val nameTv = view?.findViewById<TextView>(nameTvId)

            // Count 텍스트뷰 가져오기
            val countTvId = resources.getIdentifier("ranking_${i+1}th_count","id",requireContext().packageName)
            val countTv = view?.findViewById<TextView>(countTvId)

            // Rank 텍스트뷰 가져오기
            val rankTvId = resources.getIdentifier("ranking_${i+1}","id",requireContext().packageName)
            val rankTv = view?.findViewById<TextView>(rankTvId)

            imageView?.visibility = View.GONE
            nameTv?.visibility = View.GONE
            countTv?.visibility = View.GONE
            rankTv?.visibility = View.GONE
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun settingDate() {
        // 현재 월
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
        voteMonth.text = currentMonth.toString()
        // 오늘 날짜
        val today = LocalDate.now()
        // 이번 달의 마지막 날짜 계산
        val lastDayOfMonth = today.with(TemporalAdjusters.lastDayOfMonth())
        // 오늘로부터 이번 달이 끝날 때까지의 남은 일 수 계산
        val daysRemaining = ChronoUnit.DAYS.between(today, lastDayOfMonth)
        voteRemainDay.text = daysRemaining.toString()
    }

    private fun fanTabSetting() {
        // 뷰 나타내기
        fanAll.visibility = View.VISIBLE
        fanBest.visibility = View.VISIBLE
        // 색상 변경
        fanTab.setTextColor(Color.parseColor("#5C21A4"))
        fanTab.setBackgroundResource(R.drawable.custom_background_top)
        artistTab.setTextColor(Color.parseColor("#FFFFFFFF"))
        artistTab.setBackgroundResource(R.drawable.vote_tab_unclicked)
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
            fanTab.setTextColor(Color.parseColor("#FFFFFFFF"))
            fanTab.setBackgroundResource(R.drawable.vote_tab_unclicked)
            artistTab.setTextColor(Color.parseColor("#5C21A4"))
            artistTab.setBackgroundResource(R.drawable.custom_background_top)
        }
    }

    // db에서 토큰 가져오기
    private fun getAccessToken(): String? {
        ServiceAccessTokenDB.init(requireContext())
        val serviceTokenDB = ServiceAccessTokenDB.getInstance()
        var accessToken: String? = null

        for ((key, value) in serviceTokenDB.all) {
            if (key == "accessToken") {
                accessToken = "Bearer " + value.toString()
            }
        }
        return accessToken
    }

    // 투표권 개수 가져오기
    private suspend fun getNumberOfVotes() {
        Log.d("getNumberOfVotes 함수", "호출 성공")
        val accessToken = getAccessToken()
        try {
            if (accessToken != null) {
                val response: Response<ServerResponse<NumberOfVote>> = withContext(Dispatchers.IO) {
                    VoteCountInstance.getVotesService().getVotes(accessToken)
                }
                if (response.isSuccessful) {
                    val theVote: ServerResponse<NumberOfVote>? = response.body()
                    if (theVote != null) {
                        Log.d("response", "${theVote.statusCode} ${theVote.message}")
                        handleTheVotes(theVote)
                    } else {
                        handleError("Response body is null.")
                    }
                } else {
                    handleError("getNumverOfVotes함수 Error: ${response.code()} - ${response.message()}")
                }
            }
            }
             catch (e: Exception) {
            handleError(e.message ?: "Unknown error occurred.")
        }
    }

    // 1-4위 api
    private suspend fun getVoteSummary() {
        Log.d("getVoteSummary 함수", "호출 성공")
        val accessToken = getAccessToken()
        try {
            if (accessToken != null) {
                val response: Response<ArtistServerResponse<VotesItem>> = withContext(Dispatchers.IO) {
                    VoteSummaryInstance.voteSummaryService().getVoteSummary(accessToken)
                }
                if (response.isSuccessful) {
                    val theVote: ArtistServerResponse<VotesItem>? = response.body()
                    if (theVote != null) {
                        Log.d("response", "${theVote.statusCode} ${theVote.message}")
                        handleResponse(theVote)
                    } else {
                        handleError("Response body is null.")
                    }
                } else {
                    handleError("getVoteSummary함수 Error: ${response.code()} - ${response.message()}")
                }
            }
        }
        catch (e: Exception) {
            handleError(e.message ?: "Unknown error occurred.")
        }
    }

    // 서버에서 월간 아티스트 투표 데이터 가져오는 함수
    private suspend fun getRankingOfArtists() {
        Log.d("getRankingOfArtists 함수", "호출 성공")
        val accessToken = getAccessToken()

        try {
            // IO 스레드에서 Retrofit 호출 및 코루틴 실행
            // Retrofit을 사용해 서버에서 받아온 응답을 저장하는 변수
            // Response는 Retrofit이 제공하는 HTTP 응답 객체
            if (accessToken != null) {
                val response: Response<ArtistServerResponse<VoteData>> = withContext(Dispatchers.IO) {
                    MonthlyArtistRankingInstance.getVoteCountService().getVoteCount(accessToken)
                }
                // Response를 처리하는 코드
                if (response.isSuccessful) {
                    val artistVoteCountList: ArtistServerResponse<VoteData>? = response.body()
                    if (artistVoteCountList != null) {
                        Log.d("voteCountList", "${artistVoteCountList.statusCode} ${artistVoteCountList.message}")
                        // 정렬 기준에 따른 순서
                        handleRankingData(artistVoteCountList)
                    } else {
                        handleError("Response body is null.")
                    }
                } else {
                    Log.d("error", "서버 연동 실패")
                    handleError("getRankingOfArtists함수 Error: ${response.code()} - ${response.message()}")
                }
              }
            }
             catch (e: Exception) {
            // 예외 처리 코드
            handleError(e.message ?: "Unknown error occurred.")
        }
    }

    // 팬 순위 가져오기
    private suspend fun getRankingOfFan(type: Int) {
        Log.d("getRankingOfFan 함수", "호출 성공")
        val accessToken = getAccessToken()

        try {
            // IO 스레드에서 Retrofit 호출 및 코루틴 실행
            // Retrofit을 사용해 서버에서 받아온 응답을 저장하는 변수
            // Response는 Retrofit이 제공하는 HTTP 응답 객체
            if (accessToken != null) {
                val response: Response<ArtistServerResponse<VoteData>> = withContext(Dispatchers.IO) {
                    MonthlyFanRankingInstance.getVoteCountService().getVoteCount(accessToken, type)
                }
                // Response를 처리하는 코드
                if (response.isSuccessful) {
                    Log.d("팬랭킹 response","응답 성공")
                    val fanVoteCountList: ArtistServerResponse<VoteData>? = response.body()
                    if (fanVoteCountList != null) {
                        Log.d("voteCountList", "${fanVoteCountList.statusCode} ${fanVoteCountList.message}")
                        // 팬 순위 클릭 시 데이터 조회 (기본: 전체)
                        handleRankingData(fanVoteCountList)
                    } else {
                        handleError("Response body is null.")
                    }
                } else {
                    Log.d("error", "서버 연동 실패")
                    handleError("getRankingOfFan함수 Error: ${response.code()} - ${response.message()}")
                }
            }
        }
            catch (e: Exception) {
            // 예외 처리 코드
            handleError(e.message ?: "Unknown error occurred.")
        }
    }

    // 보유 투표권 개수 조회
    private fun handleTheVotes(votes: ServerResponse<NumberOfVote>) {
        Log.d("handleTheVotes 함수","호출 성공" )
        numberOfVote.text = votes.data.voteCount.toString()
    }

    // 1~4위 까지
    @SuppressLint("DiscouragedApi")
    private fun handleResponse(voteCountList: ArtistServerResponse<VotesItem>?) {
        val theSummary = voteCountList?.data
        if (theSummary != null) {
            for (i in theSummary.indices) {
                // 이미지뷰 가져오기
                val imageViewId = resources.getIdentifier("first_img", "id", requireContext().packageName)
                val imageView = view?.findViewById<ImageView>(imageViewId)

                // 텍스트뷰 가져오기
                val nameTvId = resources.getIdentifier("name_${i + 1}", "id", requireContext().packageName)
                val nameTv = view?.findViewById<TextView>(nameTvId)

                val countTvId = resources.getIdentifier("vote_count_${i+1}","id",requireContext().packageName)
                val countTv = view?.findViewById<TextView>(countTvId)

                // 컨테이너 가져오기
                val containerId = resources.getIdentifier("ct_${i+1}", "id", requireContext().packageName)
                val containerView = view?.findViewById<ConstraintLayout>(containerId)

                if (nameTv != null) {
                    nameTv.text = theSummary[i].name
                }
                if (countTv != null) {
                    countTv.text = addCommasToNumber(theSummary[i].voteCount.toString())
                }
                if (theSummary[i].rank == 1 && imageView != null) {
                    Glide.with(this)
                        .load(theSummary[i].thumbnailUrl)
                        .apply(RequestOptions.circleCropTransform()) // 이미지뷰 모양에 맞추기
                        .into(imageView)
                }

                // 최애 아티스트인 경우 배경 색상 변경
                if (theSummary[i].isFavorite) {
                    val drawable = containerView?.background // drawable 가져옴
                    drawable?.setColorFilter(
                        ContextCompat.getColor(requireContext(), R.color.mp),
                        PorterDuff.Mode.SRC_ATOP //색상을 적용할 때 알파 채널을 유지하도록 하는 모드
                    )
                } else {
                    // 원래의 배경색상으로 복원
                    containerView?.background?.clearColorFilter()
                }
            }
        }
    }

    // 월간 아티스트 & 팬 순위 (수정)
    @SuppressLint("DiscouragedApi", "SetTextI18n", "NotifyDataSetChanged")
    private fun handleRankingData(voteCountList: ArtistServerResponse<VoteData>?) {
        visibleOfView()
        rankingList.clear()
        if (voteCountList != null) {
            // 이미지뷰와 텍스트뷰의 인덱스 반복문으로 순회
            for (i in voteCountList.data.indices) {
                val voteCountData = voteCountList.data[i]
                val imageUrl = voteCountData.thumbnailUrl
                val nametxt = voteCountData.name
                var numberOfVotes = voteCountData.voteCount

                if (i in 0 until 3) {
                    // 이미지뷰 가져오기
                    val imageViewId = resources.getIdentifier("raking_${i+1}th_img", "id", requireContext().packageName)
                    val imageView = view?.findViewById<ImageView>(imageViewId)

                    // name 텍스트뷰 가져오기
                    val nameTvId = resources.getIdentifier("ranking_${i+1}th_name", "id", requireContext().packageName)
                    val nameTv = view?.findViewById<TextView>(nameTvId)
                    // Count 텍스트뷰 가져오기
                    val countTvId = resources.getIdentifier("ranking_${i+1}th_count","id",requireContext().packageName)
                    val countTv = view?.findViewById<TextView>(countTvId)
                    // Rank 텍스트뷰 가져오기
                    val rankTvId = resources.getIdentifier("ranking_${i+1}","id",requireContext().packageName)
                    val rankTv = view?.findViewById<TextView>(rankTvId)

                    imageView?.visibility = View.VISIBLE
                    nameTv?.visibility = View.VISIBLE
                    countTv?.visibility = View.VISIBLE
                    rankTv?.visibility = View.VISIBLE

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
                    numberOfVotes = addCommasToNumber(numberOfVotes)
                    rankingList.add(VoteRankingItem("${i+1}위",imageUrl,nametxt,numberOfVotes))
                }
            }
            // 리사이클러뷰 어댑터 연결(아이템 개수만큼 생성)
            val spanCount = rankingList.size
            rvRanking.adapter = voteRankAdapter
            val layoutManager = GridLayoutManager(context, spanCount, GridLayoutManager.HORIZONTAL, false)
            rvRanking.layoutManager = layoutManager

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
        val formattedNumber = StringBuilder()

        var count = 0
        for (i in number.length - 1 downTo 0) {
            formattedNumber.insert(0, number[i])
            count++
            if (count % 3 == 0 && i != 0) {
                formattedNumber.insert(0, ",")
            }
        }
        return formattedNumber.toString()
    }

    // 투표하기 다이얼로그를 생성
    private fun showPopupVoteDialog() {
        val remainVote = voteCount.text.toString().toInt()
        val popupVote = PopupVote(requireContext(), remainVote)
        popupVote.show()

        popupVote.setOnDismissListener {
            Log.d("VoteFragment", "PopupVote dialog dismissed.")
        }
    }

    private fun showPopupMissionDialog() {
        val popupVoteMission = PopupVoteMission(requireContext())
        popupVoteMission.show()

        popupVoteMission.setOnDismissListener {
            Log.d("popupVoteMission", "PopupVote dialog dismissed.")
        }
    }
}