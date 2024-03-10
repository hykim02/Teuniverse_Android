package com.example.teuniverse

import PopupVoteCheck
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.teuniverse.databinding.FragmentHomeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class HomeFragment : Fragment(), PopupVoteCheck.VoteMissionListener {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var scheduleList: ArrayList<Event>
    private lateinit var mediaAdapter: HomeMediaAdapter
    private lateinit var mediaList: ArrayList<HomeMediaItem>
    private lateinit var communityAdapter: HomeCommunityAdapter
    private lateinit var communityList: ArrayList<HomeCommunityItem>
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding =  FragmentHomeBinding.inflate(inflater, container, false)

        binding.imgBtnVote.setOnClickListener {
            showPopupMissionDialog()
        }

        binding.voteBtn.setOnClickListener {
            showPopupVoteDialog()
        }

        // 출석 체크 3표 지급(1회)
        lifecycleScope.launch {
            voteMissionApi(3, 1)
        }

        // 일정 리사이클러뷰 어댑터 연결
        scheduleList = ArrayList()
        calendarAdapter = CalendarAdapter(scheduleList)
        // 커뮤니티 리사이클러뷰 어댑터 연결
        val navController = findNavController()
        communityList = ArrayList()
        communityAdapter = HomeCommunityAdapter(communityList, navController)
        // 미디어 리사이클러뷰 어댑터 연결
        mediaList = ArrayList()
        mediaAdapter = HomeMediaAdapter(mediaList)

        navigate()

        lifecycleScope.launch {
            homeApi()
            getNumberOfVotes()
        }

        return binding.root
    }

    // 화면 전환 함수
    private fun navigate() {
        // 투표 화면
        binding.toVote.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_vote)
        }
        binding.toVoteBtn.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_vote)
        }

        // 일정 화면
        binding.toCalendar.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_calendar)
        }
        binding.toCalendarBtn.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_calendar)
        }

        // 미디어 화면
        binding.toMedia.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_media)
        }
        binding.toMediaBtn.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_media)
        }

        // 커뮤니티 화면
        binding.toCommunity.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_community)
        }
        binding.toCommunityBtn.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_community)
        }

        // 프로필
        binding.imgBtnPerson.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_profileFragment)
        }
    }

    // 홈 api
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun homeApi() {
        Log.d("homeApi", "호출 성공")
        val accessToken = getAccessToken()
        try {
            if (accessToken != null) {
                val response: Response<ServerResponse<HomeItem>> = withContext(
                    Dispatchers.IO) {
                    HomeInstance.homeService().getHomeContents(accessToken)
                }
                if (response.isSuccessful) {
                    val theContents: ServerResponse<HomeItem>? = response.body()
                    if (theContents != null) {
                        Log.d("homeApi", "${theContents.statusCode} ${theContents.message}")
                        handleResponse(theContents)
                    } else {
                        handleError("Response body is null.")
                    }
                } else {
                    handleError("homeApi Error: ${response.code()} - ${response.message()}")
                }
            }
        }
        catch (e: Exception) {
            handleError(e.message ?: "Unknown error occurred.")
        }
    }

    // 투표권 지급 미션 api
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun voteMissionApi(voteCount: Int, type: Int) {
        Log.d("voteMissionApi", "호출 성공")
        val accessToken = getAccessToken()
        try {
            if (accessToken != null) {
                val response: Response<ServerResponse<NumberOfVote>> = withContext(
                    Dispatchers.IO) {
                    GiveVoteInstance.giveVoteService().giveVote(accessToken, voteCount, type)
                }
                if (response.isSuccessful) {
                    val theVotes: ServerResponse<NumberOfVote>? = response.body()
                    if (theVotes != null) {
                        Toast.makeText(requireContext(), "일일미션 출석체크 완료", Toast.LENGTH_SHORT).show()
                        Log.d("homeApi", "${theVotes.statusCode} ${theVotes.message}")
                        binding.voteCount.text = theVotes.data.voteCount.toString()
                    } else {
                        Toast.makeText(requireContext(), "일일미션 출석체크 실패", Toast.LENGTH_SHORT).show()
                        handleError("Response body is null.")
                    }
                } else {
                    Toast.makeText(requireContext(), "일일미션 출석체크 실패", Toast.LENGTH_SHORT).show()
                    handleError("homeApi Error: ${response.code()} - ${response.message()}")
                }
            }
        }
        catch (e: Exception) {
            handleError(e.message ?: "Unknown error occurred.")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleResponse(response: ServerResponse<HomeItem>) {
        // 투표
        val votes = response.data.votes
        val names = arrayOf(binding.name1, binding.name2, binding.name3, binding.name4)
        val voteCount = arrayOf(binding.voteCount1, binding.voteCount2, binding.voteCount3, binding.voteCount4)
        for (i in votes.indices) {
            if (i == 0) {
                Glide.with(this)
                    .load(votes[i].thumbnailUrl)
                    .apply(RequestOptions.circleCropTransform()) // 이미지뷰 모양에 맞추기
                    .into(binding.firstImg)
            }
            names[i].text = votes[i].name
            voteCount[i].text = addCommasToNumber(votes[i].voteCount.toString())

            // 컨테이너 가져오기
            val containerId = resources.getIdentifier("ct_${i+1}", "id", requireContext().packageName)
            val containerView = view?.findViewById<ConstraintLayout>(containerId)

            // 최애 아티스트인 경우 배경 색상 변경
            if (votes[i].isFavorite) {
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

        // 일정
        val schedules = response.data.schedules.values
        scheduleList.clear()
        schedules.forEachIndexed { _, events ->
            for (i in events.indices) {
                val content = events[i].content
                val startAt = setTime(events[i].startAt)
                val type = setTypeImg(events[i].type)

                scheduleList.add(Event(content, type.toString(), startAt))
            }
            // 리사이클러뷰 어댑터 연결(아이템 개수만큼 생성)
            val spanCount = scheduleList.size
            val layoutManager = GridLayoutManager(context, spanCount, GridLayoutManager.HORIZONTAL, false)
            binding.scheduleRv.adapter = calendarAdapter
            binding.scheduleRv.layoutManager = layoutManager

            // 어댑터에 데이터가 변경되었음을 알리기
            calendarAdapter.notifyDataSetChanged()
        }

        // 커뮤니티
        val communities = response.data.communities
        communityList.clear()
        for (i in communities.indices) {
            val id = communities[i].id
            val content = getContextPreview(communities[i].content)
            val thumbnailUrl = communities[i].thumbnailUrl

            communityList.add(HomeCommunityItem(id, content, thumbnailUrl))
            val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            binding.rvHomeCommunity.adapter = communityAdapter
            binding.rvHomeCommunity.layoutManager = layoutManager

            // 어댑터에 데이터가 변경되었음을 알리기
            communityAdapter.notifyDataSetChanged()
        }


        // 미디어
        val medias = response.data.medias
        mediaList.clear()
        for (i in medias.indices) {
            val thumbnailUrl = medias[i].thumbnailUrl
            val url = medias[i].url

            mediaList.add(HomeMediaItem(url, thumbnailUrl))
            val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            binding.rvHomeMedia.adapter = mediaAdapter
            binding.rvHomeMedia.layoutManager = layoutManager

            // 어댑터에 데이터가 변경되었음을 알리기
            mediaAdapter.notifyDataSetChanged()
        }
    }

    private fun setTypeImg(type: String): Int {
        if (type == "기념일") {
            return R.drawable.cake_on
        } else if (type == "행사") {
            return R.drawable.festival_on
        } else if (type == "방송") {
            return R.drawable.video_on
        } else { // 기타
            return R.drawable.more_on
        }
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

    // 보유 투표권 개수 조회
    private fun handleTheVotes(votes: ServerResponse<NumberOfVote>) {
        Log.d("handleTheVotes 함수","호출 성공" )
        binding.voteCount.text = votes.data.voteCount.toString()
    }

    // 일정 시간 추출 함수
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setTime(time: String): String {
        // DateTimeFormatter를 사용하여 문자열을 LocalDateTime으로 파싱
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val dateTime = LocalDateTime.parse(time, formatter)

        // LocalDateTime에서 시간과 분 추출
        val hour = dateTime.hour.toString()
        val minute = dateTime.minute.toString()

        // 추출된 시간과 분을 2자리 숫자로 변환하여 변수에 저장
        val extractedHour = String.format("%02d", hour.toInt())
        val extractedMinute = String.format("%02d", minute.toInt())

        return "$extractedHour:$extractedMinute"
    }

    //요약 보여주기 - 공지 전체내용 중 일부(15자)만 가져옴
    //15자 이상인 경우 일부를 자르고 "..."을 붙임
    private fun getContextPreview(context: String): String {
        return if (context.isNotEmpty()) {
            val trimmedText = if (context.length > 6) "${context.substring(0, 5)}..." else context
            trimmedText
        } else {
            ""
        }
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

    private fun handleError(errorMessage: String) {
        // 에러를 처리하는 코드
        Log.d("홈Api Error", errorMessage)
    }

    // db에서 토큰 가져오기
    private fun getAccessToken(): String? {
        MainActivity.ServiceAccessTokenDB.init(requireContext())
        val serviceTokenDB = MainActivity.ServiceAccessTokenDB.getInstance()
        var accessToken: String? = null

        for ((key, value) in serviceTokenDB.all) {
            if (key == "accessToken") {
                accessToken = "Bearer " + value.toString()
            }
        }
        return accessToken
    }

    private fun showPopupMissionDialog() {
        val popupVoteMission = PopupVoteMission(requireContext())
        popupVoteMission.show()

        popupVoteMission.setOnDismissListener {
            Log.d("popupVoteMission", "PopupVote dialog dismissed.")
        }
    }

    // 투표하기 다이얼로그를 생성
    private fun showPopupVoteDialog() {
        val popupVote = PopupVote(requireContext()) { /* Callback if needed */ }
        popupVote.show()

        popupVote.setOnDismissListener {
            Log.d("VoteFragment", "PopupVote dialog dismissed.")
        }
    }

    // 인터페이스 구현
    override fun giveVote(voteCount: Int) {
        binding.voteCount.text = voteCount.toString()
    }

}