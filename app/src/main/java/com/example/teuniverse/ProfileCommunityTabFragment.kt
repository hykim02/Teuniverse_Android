package com.example.teuniverse

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.view.size
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.request.RequestOptions
import com.example.teuniverse.databinding.FragmentProfileCommunityTabBinding
import com.kakao.sdk.user.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.concurrent.TimeUnit

class ProfileCommunityTabFragment : Fragment() {
    private lateinit var binding: FragmentProfileCommunityTabBinding
    private lateinit var feedList: ArrayList<CommunityPostItem>
    private lateinit var communityAdapter: CommunityPostAdapter

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileCommunityTabBinding.inflate(inflater, container, false)

        feedList = ArrayList()
        // 어댑터에 NavController 전달
        val navController = findNavController()
        communityAdapter = CommunityPostAdapter(feedList, navController, viewLifecycleOwner)
        // 리사이클러뷰 어댑터 연결
        binding.tabCommunityRv.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.tabCommunityRv.adapter = communityAdapter

        lifecycleScope.launch {
            communityFeedsApi()
        }

        return binding.root
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
        val feedsData = theFeeds.data.feeds
        UserInfoDB.init(requireContext())
        val userInfoData = UserInfoDB.getInstance().all
        val userID = userInfoData.getValue("id")

        for (i in feedsData.indices) {
            val feed = feedsData[i]
            if (feed.userProfile.id == userID) {
                // user
                val userProfileData = feed.userProfile
                val userName = userProfileData.nickName
                val userImg = userProfileData.thumbnailUrl
                // feed
                val feedId = feed.id
                val feedImg = feed.thumbnailUrl
                var feedContent = feed.content
                val heartCount = feed.likeCount
                val param = dateTimeToMillSec(feed.createdAt)
                val time = calculationTime(param)
                val commentCount = feed.commentCount

                feedList.add(
                    CommunityPostItem(
                        feedId, userImg, userName,time,feedImg,feedContent,heartCount,commentCount))
            }
        }
        communityAdapter.notifyDataSetChanged()

        if (feedList.size >= 1) { // 내가 작성한 게시글이 하나라도 있다면
            binding.tabCommunityRv.visibility = View.VISIBLE
            binding.tv.visibility = GONE
            binding.tv2.visibility = GONE
            // 어댑터에 데이터가 변경되었음을 알리기
            communityAdapter.notifyDataSetChanged()
        } else { // 내가 작성한 게시글이 하나도 없다면
            binding.tabCommunityRv.visibility = GONE
            binding.tv.visibility = View.VISIBLE
            binding.tv2.visibility = View.VISIBLE
        }
    }

    // 서버 시간 가져와서 초로 계산
    @SuppressLint("SimpleDateFormat")
    private fun dateTimeToMillSec(serverTime: String): Long{
        var timeInMilliseconds: Long = 0
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        try {
            val mDate = sdf.parse(serverTime)
            timeInMilliseconds = mDate.time
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return timeInMilliseconds
    }

    // 현재 시간 가져온 후 서버 시간과 비교
    private fun calculationTime(createDateTime: Long): String{
        val nowDateTime = Calendar.getInstance().timeInMillis //현재 시간 to millisecond
        var value = ""
        val differenceValue = nowDateTime - createDateTime //현재 시간 - 비교가 될 시간
        when {
            differenceValue < 60000 -> { //59초 보다 적다면
                value = "방금 전"
            }
            differenceValue < 3600000 -> { //59분 보다 적다면
                value =  TimeUnit.MILLISECONDS.toMinutes(differenceValue).toString() + "분 전"
            }
            differenceValue < 86400000 -> { //23시간 보다 적다면
                value =  TimeUnit.MILLISECONDS.toHours(differenceValue).toString() + "시간 전"
            }
            differenceValue <  604800000 -> { //7일 보다 적다면
                value =  TimeUnit.MILLISECONDS.toDays(differenceValue).toString() + "일 전"
            }
            differenceValue < 2419200000 -> { //3주 보다 적다면
                value =  (TimeUnit.MILLISECONDS.toDays(differenceValue)/7).toString() + "주 전"
            }
            differenceValue < 31556952000 -> { //12개월 보다 적다면
                value =  (TimeUnit.MILLISECONDS.toDays(differenceValue)/30).toString() + "개월 전"
            }
            else -> { //그 외
                value =  (TimeUnit.MILLISECONDS.toDays(differenceValue)/365).toString() + "년 전"
            }
        }
        return value
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

    private fun handleError(errorMessage: String) {
        // 에러를 처리하는 코드
        Log.d("communityFeedsApi 함수 Error", errorMessage)
    }
}