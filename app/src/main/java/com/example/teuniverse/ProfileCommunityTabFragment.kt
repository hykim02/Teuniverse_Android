package com.example.teuniverse

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
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
        MainActivity.UserInfoDB.init(requireContext())
        val userInfoData = MainActivity.UserInfoDB.getInstance().all
        val userID = userInfoData.getValue("id")

        for (i in feedsData.indices) {
            val feed = feedsData[i]
            if (feed.userProfile.id == userID) {
                binding.tabCommunityRv.visibility = View.VISIBLE
                // user
                val userProfileData = feed.userProfile
                val userName = userProfileData.nickName
                val userImg = userProfileData.thumbnailUrl
                // feed
                val feedId = feed.id
                val feedImg = feed.thumbnailUrl
                var feedContent = feed.content
                val heartCount = feed.likeCount
                val time = "11분 전"
                val commentCount = feed.commentCount
                setTime(feed.createdAt)

                feedList.add(
                    CommunityPostItem(
                        feedId, userImg, userName,time,feedImg,feedContent,heartCount,commentCount))
            } else { // 작성한 게시물이 없는 경우
                binding.tabCommunityRv.visibility = GONE

                // TextView 생성 및 텍스트 설정
                val textView = TextView(requireContext())
                textView.text = "아직 작성한 게시물이 없습니다"
                textView.setTextColor(Color.parseColor("#7C7C7C"))
                textView.textSize = 16f
                val textView2 = TextView(requireContext())
                textView2.text = "커뮤니티에서 글을 작성해보세요!"
                textView2.setTextColor(Color.parseColor("#1A1836"))
                textView.textSize = 20f

                // 생성한 TextView 레이아웃에 추가
                val containerLayout = view?.findViewById<ViewGroup>(R.id.tab_ct)
                containerLayout?.addView(textView)
                containerLayout?.addView(textView2)

            }
        }
        // 어댑터에 데이터가 변경되었음을 알리기
        communityAdapter.notifyDataSetChanged()
    }

    // 일정 시간 추출 함수
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setTime(time: String){
        // DateTimeFormatter를 사용하여 문자열을 LocalDateTime으로 파싱
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        val dateTime = LocalDateTime.parse(time, formatter)
        val currentTime : Long = System.currentTimeMillis() // ms로 반환

        // LocalDateTime에서 시간과 분 추출
        val hour = dateTime.hour.toString()
        val minute = dateTime.minute.toString()
        val month = dateTime.monthValue
        val day = dateTime.dayOfMonth
        val dataFormat5 = SimpleDateFormat("yyyy-MM-dd-hh:mm:ss")
        Log.d("서버 시간", "$hour-$minute-$month-$day")
        Log.d("로컬 시간", dataFormat5.format(currentTime))
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
}