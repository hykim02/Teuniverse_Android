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
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var calendarAdapter: CalendarAdapter
    private lateinit var scheduleList: ArrayList<Event>
    private lateinit var mediaAdapter: HomeMediaAdapter
    private lateinit var mediaList: ArrayList<HomeMediaItem>
    private lateinit var communityAdapter: HomeCommunityAdapter
    private lateinit var communityList: ArrayList<HomeCommunityItem>
    private lateinit var bottomNavView: BottomNavigationView
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
            val count = binding.voteCount.text.toString()

            if(count.toInt() >= 1) {
                showPopupVoteDialog()
            } else {
                Toast.makeText(context, "보유한 투표권이 없습니다", Toast.LENGTH_SHORT).show()
            }
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
        bottomNavView = requireActivity().findViewById(R.id.bottom_navigation_view)

        navigate()
        setSchedule() // 일정 데이터

        VoteMissionDB.init(requireContext())
        val db = VoteMissionDB.getInstance()
        val count = db.getInt("attend", 0)

        lifecycleScope.launch {
            homeApi()
            getNumberOfVotes()

            if(count == 0) {
                voteMissionApi(3, 1, count) // 출석 체크 3표 지급(1회)
            }
        }

        return binding.root
    }

    private fun setSchedule() {
        Log.d("setSchedule","실행")
        UserInfoDB.init(requireContext())
        val db = UserInfoDB.getInstance().all
        val editor = UserInfoDB.getInstance().edit()
        val hasChanged = db.getValue("edit")
        Log.d("hasChanged", hasChanged.toString())

        if(hasChanged == 1) {
            Log.d("hasChanged","1 실행")
            clearSchedule()
            callSchedule(2024)
            editor.putInt("edit", 0)
            editor.apply()
        } else {
            Log.d("hasChanged","0 실행")
            callSchedule(2024)
        }
    }

    // 일정 초기화
    private fun clearSchedule() {
        Log.d("일정", "초기화")
        for(i in 1 .. 12) {
            val isExist = MonthDBManager.doesSharedPreferencesFileExist(requireContext(), i.toString())
            if(isExist) {
                Log.d("${i}월", "초기화")
                val editor = MonthDBManager.getMonthInstance(i).edit()
                editor.clear()
                editor.apply()
            } else {
                continue
            }
        }
    }

    // 1-12월까지 api 모두 호출
    private fun callSchedule(year: Int) {
        for (i in 1..12) {
            lifecycleScope.launch {
                scheduleApi(year, i)
            }
        }
    }

    // 화면 전환 함수
    private fun navigate() {
        // 투표 화면
        binding.toVote.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_vote)
            // 프로필 탭의 아이디로 선택된 아이템을 변경
            bottomNavView.selectedItemId = R.id.navigation_vote
        }
        binding.toVoteBtn.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_vote)
            bottomNavView.selectedItemId = R.id.navigation_vote
        }

        // 일정 화면
        binding.toCalendar.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_calendar)
            bottomNavView.selectedItemId = R.id.navigation_calendar
        }
        binding.toCalendarBtn.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_calendar)
            bottomNavView.selectedItemId = R.id.navigation_calendar
        }

        // 미디어 화면
        binding.toMedia.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_media)
            bottomNavView.selectedItemId = R.id.navigation_media
        }
        binding.toMediaBtn.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_media)
            bottomNavView.selectedItemId = R.id.navigation_media
        }

        // 커뮤니티 화면
        binding.toCommunity.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_community)
            bottomNavView.selectedItemId = R.id.navigation_community
        }
        binding.toCommunityBtn.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_community)
            bottomNavView.selectedItemId = R.id.navigation_community
        }

        // 프로필
        binding.imgBtnPerson.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_profileFragment)
            bottomNavView.selectedItemId = 0
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
    private suspend fun voteMissionApi(voteCount: Int, type: Int, count: Int) {
        Log.d("voteMissionApi", "호출 성공")
        val accessToken = getAccessToken()
        val params = VoteMission(voteCount = voteCount, type = type)
        try {
            if (accessToken != null) {
                val response: Response<ServerResponse<NumberOfVote>> = withContext(
                    Dispatchers.IO) {
                    GiveVoteInstance.giveVoteService().giveVote(accessToken, params)
                }
                if (response.isSuccessful) {
                    val theVotes: ServerResponse<NumberOfVote>? = response.body()
                    if (theVotes != null) {
                        Toast.makeText(requireContext(), "일일미션 출석체크 완료(${count+1}회)", Toast.LENGTH_SHORT).show()
                        Log.d("homeApi", "${theVotes.statusCode} ${theVotes.message}")
                        handleMission(theVotes)
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

    private fun handleMission(theVotes: ServerResponse<NumberOfVote>?) {
        VoteMissionDB.init(requireContext())
        val editor = VoteMissionDB.getInstance().edit()

        if (theVotes != null) {
            binding.voteCount.text = theVotes.data.voteCount.toString()
            editor.putInt("attend", 1)
            editor.apply()
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

        val localDateTime: LocalDateTime = LocalDateTime.now() //2024-03-14

        // 일정
        val schedules = response.data.schedules.values
        binding.noSchedule.visibility = View.GONE
        scheduleList.clear()

        if(schedules.isEmpty()) {
            binding.noSchedule.visibility = View.VISIBLE
            binding.scheduleRv.visibility = View.GONE
        } else {
            binding.scheduleRv.visibility = View.VISIBLE
            schedules.forEachIndexed { _, events ->
                for (i in events.indices) {
                    val content = events[i].content
                    val server = setDate(events[i].startAt) // 서버 날짜 ex)14
                    val local = localDateTime.dayOfMonth // 로컬 날짜 ex)14
                    val startAt = setTime(events[i].startAt)
                    val type = setTypeImg(server, local, startAt)

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
        }

        // 커뮤니티
        val communities = response.data.communities
        binding.noCommunity.visibility = View.GONE
        communityList.clear()

        if(communities.isEmpty()) {
            binding.noCommunity.visibility = View.VISIBLE
            binding.rvHomeCommunity.visibility = View.GONE
        } else {
            binding.rvHomeCommunity.visibility = View.VISIBLE

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
        }


        // 미디어
        val medias = response.data.medias
        binding.noMedia.visibility = View.GONE
        mediaList.clear()

        if(medias.isEmpty()) {
            binding.noMedia.visibility = View.VISIBLE
            binding.rvHomeMedia.visibility = View.GONE
        } else {
            binding.rvHomeMedia.visibility = View.VISIBLE

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
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setTypeImg(server: Int, local: Int, startAt: String): Int {
        val currentTime : Long = System.currentTimeMillis()
        val localTime = SimpleDateFormat("HH:mm").format(currentTime).split(":") //12:05
        val serverTime = startAt.split(":")

        if(local == server) {
            // 일정이 진행되기 전
            if((localTime[0] < serverTime[0]) ||
                (localTime[0] == serverTime[0]) && (localTime[1] < serverTime[1])) {
                return R.drawable.today_colored
            } else {
                return R.drawable.today
            }
        } else {
            return R.drawable.tomorrow
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

    // 1-12월 일정 받아오기
    private suspend fun scheduleApi(year: Int, month: Int) {
        Log.d("scheduleApi $month", "호출 성공")
        val accessToken = getAccessToken()
        try {
            if (accessToken != null) {
                val response: Response<EventResponse> = withContext(
                    Dispatchers.IO) {
                    CalendarInstance.scheduleService().getSchedule(year, month, accessToken)
                }
                if (response.isSuccessful) {
                    val theSchedule: EventResponse? = response.body()
                    if (theSchedule != null) {
                        Log.d("scheduleApi $month", "${theSchedule.statusCode} ${theSchedule.message}")
                        handleResponse(theSchedule, month)
                    } else {
                        handleError("Response body is null.",month)
                    }
                } else {
                    handleError("scheduleApi $month Error: ${response.code()} - ${response.message()}",month)
                }
            }
        }
        catch (e: Exception) {
            handleError(e.message ?: "Unknown error occurred.",month)
        }
    }

    private fun handleResponse(response: EventResponse, month: Int) {
        MonthDBManager.initMonth(requireContext(), month) // 초기화
        val monthDB = MonthDBManager.getMonthInstance(month) // 객체 얻기

        // DB에 데이터 저장
        if (response.data.isNotEmpty()) {
            Log.d("일정api", response.data.toString())
            response.data.forEach { (date, events) ->
                val jsonString = Gson().toJson(events) // Convert events List to JSON String
                with(monthDB.edit()) {
                    putString(date, jsonString)
                    apply()
                }
            }
        }
    }

    private fun handleError(errorMessage: String, month: Int) {
        // 에러를 처리하는 코드
        Log.d("일정Api $month Error", errorMessage)
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

    // 날짜 부분만 추출
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setDate(dateTimeString: String): Int {
        // ISO 8601 형식의 문자열을 LocalDateTime 객체로 변환
        val dateTime = LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_DATE_TIME)

        return dateTime.toLocalDate().dayOfMonth //14
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

    private fun showPopupMissionDialog() {
        val popupVoteMission = PopupVoteMission(requireContext())
        popupVoteMission.show()

        popupVoteMission.setOnDismissListener {
            Log.d("popupVoteMission", "PopupVote dialog dismissed.")
        }
    }

    // 투표하기 다이얼로그를 생성
    private fun showPopupVoteDialog() {
        val remainVote = binding.voteCount.text.toString().toInt()
        val popupVote = PopupVote(requireContext(), remainVote)
        popupVote.show()

        popupVote.setOnDismissListener {
            Log.d("VoteFragment", "PopupVote dialog dismissed.")
        }
    }
}