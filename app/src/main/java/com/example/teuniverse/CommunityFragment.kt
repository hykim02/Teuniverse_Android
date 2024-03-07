package com.example.teuniverse

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class CommunityFragment : Fragment() {

    private lateinit var rvCommunity: RecyclerView
    private lateinit var feedList: ArrayList<CommunityPostItem>
    private lateinit var communityAdapter: CommunityPostAdapter
    private lateinit var artistProfile: ImageView
    private lateinit var artistName: TextView
    private lateinit var numberOfVote: TextView
    private lateinit var intent: Intent
    private lateinit var postBtn: FloatingActionButton
    private lateinit var profileBtn: ImageButton
    private lateinit var voteBtn: ImageButton

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 코루틴을 사용하여 getArtistList 함수 호출
        lifecycleScope.launch {
            communityFeedsApi()
            getNumberOfVotesApi()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_community, container, false)

        artistProfile = view.findViewById(R.id.img_best_artist)
        artistName = view.findViewById(R.id.tv_best_artist_name)
        rvCommunity = view.findViewById(R.id.rv_post)
        feedList = ArrayList()
        numberOfVote = view.findViewById(R.id.vote_count)
        postBtn = view.findViewById(R.id.add_btn)
        voteBtn = view.findViewById(R.id.img_btn_vote)

        // 어댑터에 NavController 전달
        val navController = findNavController()
        communityAdapter = CommunityPostAdapter(feedList, navController, viewLifecycleOwner)
        // 리사이클러뷰 어댑터 연결
        rvCommunity.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rvCommunity.adapter = communityAdapter

        profileBtn = view.findViewById(R.id.img_btn_person)
        profileBtn.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_community_to_profileFragment)
        }

        postBtn.setOnClickListener {
            activity?.let{
                intent = Intent(context, CommunityPostActivity::class.java)
                startActivity(intent)
            }
        }

        voteBtn.setOnClickListener {
            showPopupMissionDialog()
        }

        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun communityFeedsApi() {
        Log.d("communityFeedsApi 함수", "호출 성공")
        val accessToken = getAccessToken()
        try {
            if (accessToken != null) {
                val response: Response<ServerResponse<CommunityData>> = withContext(Dispatchers.IO) {
                    CommunityFeedsInstance.communityFeedsService().getFeeds(accessToken)
                }
                if (response.isSuccessful) {
                    val theFeeds: ServerResponse<CommunityData>? = response.body()
                    if (theFeeds != null) {
                        Log.d("communityFeedsApi 함수 response", "${theFeeds.statusCode} ${theFeeds.message}")
                        handleGetFeeds(theFeeds)
                    } else {
                        handleError("Response body is null.")
                    }
                } else {
                    handleError("communityFeedsApi 함수 Error2: ${response.code()} - ${response.message()}")
                }
            }
        }
        catch (e: Exception) {
            handleError(e.message ?: "Unknown error occurred.")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleGetFeeds(theFeeds: ServerResponse<CommunityData>) {
        Log.d("handleGetFeeds함수","호출 성공")
        val artistProfileData = theFeeds.data.artistProfile
        val feedsData = theFeeds.data.feeds
        val dataFormat5 = SimpleDateFormat("yyyy-MM-dd-HH:mm:ss")
        val localTime = dataFormat5.format(System.currentTimeMillis())

        // 아티스트 데이터
        Glide.with(this)
            .load(artistProfileData.thumbnailUrl)
            .apply(RequestOptions.circleCropTransform()) // 이미지뷰 모양에 맞추기
            .into(artistProfile)

        artistName.text = artistProfileData.name

        for (i in feedsData.indices) {
            val feed = feedsData[i]
            // user
            val userProfileData = feed.userProfile
            val userName = userProfileData.nickName
            val userImg = userProfileData.thumbnailUrl
            // feed
            val feedId = feed.id
            val feedImg = feed.thumbnailUrl
            var feedContent = feed.content
            val heartCount = feed.likeCount
            val time = setTime(feed.createdAt)
            val commentCount = feed.commentCount
            Log.d("게시물 시간", time)

            feedList.add(
                CommunityPostItem(
                     feedId, userImg, userName,time,feedImg,feedContent,heartCount,commentCount))
        }
        // 어댑터에 데이터가 변경되었음을 알리기
        communityAdapter.notifyDataSetChanged()
    }

    // 시간 설정
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setTime(serverTime: String): String {
        // 서버에서 받아온 시간 문자열
        val serverDateTime = LocalDateTime.parse(serverTime, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
        Log.d("serverTime", serverDateTime.toString())
        // 현재 로컬 시간 가져오기
        val localDateTime = LocalDateTime.now()
        Log.d("localTime", localDateTime.toString())
        // 두 시간 간의 차이 계산
        val duration = Duration.between(localDateTime, serverDateTime)

        // 차이를 초, 분, 시간, 일, 월, 년으로 계층적으로 나누어 표현
        return when {
            duration.seconds < 60 -> "${duration.seconds}초 전"
            duration.toMinutes() < 60 -> "${duration.toMinutes()}분 전"
            duration.toHours() < 24 -> "${duration.toHours()}시간 전"
            duration.toDays() < 31 -> "${duration.toDays()}일 전"
            duration.toDays() < 365 -> "${duration.toDays() / 30}개월 전"
            else -> "${duration.toDays() / 365}년 전"
        }
    }

    // 일정 시간 추출 함수
//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun setTime(time: String){
//        // DateTimeFormatter를 사용하여 문자열을 LocalDateTime으로 파싱
//        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
//        val dateTime = LocalDateTime.parse(time, formatter)
//        val currentTime : Long = System.currentTimeMillis() // ms로 반환
//
//        // LocalDateTime에서 시간과 분 추출
//        val year = dateTime.year.toString()
//        val hour = dateTime.hour.toString()
//        val minute = dateTime.minute.toString()
//        val month = dateTime.monthValue
//        val day = dateTime.dayOfMonth
//        val second = dateTime.second.toString()
//        val dataFormat5 = SimpleDateFormat("yyyy-M-d-HH:mm:ss")
//        Log.d("서버 시간", "$year-$month-$day-$hour:$minute:$second") // 2024-3-7-23:5:2
//        Log.d("로컬 시간", dataFormat5.format(currentTime)) // 2024-3-7-14:44:56
//    }

    // 초 단위
    @RequiresApi(Build.VERSION_CODES.O)
    private fun timeSecond(localTime: String, serverTime: String): Int {
        // DateTimeFormatter를 사용하여 문자열을 LocalDateTime으로 파싱
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val dateTime = LocalDateTime.parse(serverTime, formatter)
        val serverSecond = dateTime.second
        val localSecond = localTime.split(":")[2].toInt()
        Log.d("초 단위", "$serverSecond-$localSecond")

        return localSecond - serverSecond
    }

    // 분 단위
    @RequiresApi(Build.VERSION_CODES.O)
    private fun timeMinute(localTime: String, serverTime: String): Int {
        // DateTimeFormatter를 사용하여 문자열을 LocalDateTime으로 파싱
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val dateTime = LocalDateTime.parse(serverTime, formatter)
        val serverMinute = dateTime.minute
        val localMinute = localTime.split(":")[1].toInt()
        Log.d("분 단위", "$serverMinute-$localMinute")

        return localMinute - serverMinute
    }

    // 시간 단위
    @RequiresApi(Build.VERSION_CODES.O)
    private fun timeHour(localTime: String, serverTime: String): Int {
        // DateTimeFormatter를 사용하여 문자열을 LocalDateTime으로 파싱
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val dateTime = LocalDateTime.parse(serverTime, formatter)
        val serverHour = dateTime.hour
        val splt = localTime.split(":")[0]
        val localHour = splt.split("-")[3].toInt()
        Log.d("시간 단위", "$serverHour-$localHour")

        return localHour - serverHour
    }

    // day 단위
    @RequiresApi(Build.VERSION_CODES.O)
    private fun timeDay(localTime: String, serverTime: String): Int {
        // DateTimeFormatter를 사용하여 문자열을 LocalDateTime으로 파싱
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val dateTime = LocalDateTime.parse(serverTime, formatter)
        val serverDay = dateTime.hour
        val splt = localTime.split(":")[0]
        val localDay = splt.split("-")[2].toInt()
        Log.d("day 단위", "$serverDay-$localDay")

        return localDay - serverDay
    }

    // month 단위
    @RequiresApi(Build.VERSION_CODES.O)
    private fun timeMonth(localTime: String, serverTime: String): Int {
        // DateTimeFormatter를 사용하여 문자열을 LocalDateTime으로 파싱
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val dateTime = LocalDateTime.parse(serverTime, formatter)
        val serverMonth = dateTime.hour
        val splt = localTime.split(":")[0]
        val localMonth = splt.split("-")[1].toInt()
        Log.d("Month 단위", "$serverMonth-$localMonth")

        return localMonth - serverMonth
    }

    // year 단위
    @RequiresApi(Build.VERSION_CODES.O)
    private fun timeYear(localTime: String, serverTime: String): Int {
        // DateTimeFormatter를 사용하여 문자열을 LocalDateTime으로 파싱
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val dateTime = LocalDateTime.parse(serverTime, formatter)
        val serverYear = dateTime.hour
        val splt = localTime.split(":")[0]
        val localYear = splt.split("-")[0].toInt()
        Log.d("Year 단위", "$serverYear-$localYear")

        return localYear - serverYear
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

    private fun handleError(errorMessage: String) {
        // 에러를 처리하는 코드
        Log.d("communityFeedsApi 함수 Error", errorMessage)
    }

    // 보유 투표권 조회 api
    private suspend fun getNumberOfVotesApi() {
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
        numberOfVote.text = votes.data.voteCount.toString()
    }

    private fun showPopupMissionDialog() {
        val popupVoteMission = PopupVoteMission(requireContext())
        popupVoteMission.show()

        popupVoteMission.setOnDismissListener {
            Log.d("popupVoteMission", "PopupVote dialog dismissed.")
        }
    }
}


