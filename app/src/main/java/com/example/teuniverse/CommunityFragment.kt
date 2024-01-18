package com.example.teuniverse

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
import com.example.teuniverse.databinding.PopupVoteCheckBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class CommunityFragment : Fragment() {

    private lateinit var rvCommunity: RecyclerView
    private lateinit var feedList: ArrayList<CommunityPostItem>
    private lateinit var communityAdapter: CommunityPostAdapter
    private lateinit var artistProfile: ImageView
    private lateinit var artistName: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 코루틴을 사용하여 getArtistList 함수 호출
        lifecycleScope.launch {
            communityFeedsApi()
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
        communityAdapter = CommunityPostAdapter(feedList)

        return view
    }

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
                        Log.d("response",theFeeds.toString())
                        handleGetFeeds(theFeeds)
                    } else {
                        handleError("Response body is null.")
                    }
                } else {
                    handleError("communityFeedsApi 함수 Error: ${response.code()} - ${response.message()}")
                }
            }
        }
        catch (e: Exception) {
            handleError(e.message ?: "Unknown error occurred.")
        }
    }

    private fun handleGetFeeds(theFeeds: ServerResponse<CommunityData>) {
        Log.d("handleGetFeeds함수","호출 성공")
        val artistProfileData = theFeeds.data.artistProfile
        val feedsData = theFeeds.data.feeds

        Log.d("artistProfile",artistProfileData.toString())
        // 아티스트 데이터
        Glide.with(this)
            .load(artistProfileData.thumbnailUrl)
            .apply(RequestOptions.circleCropTransform()) // 이미지뷰 모양에 맞추기
            .into(artistProfile)

        artistName.text = artistProfileData.name

        Log.d("for문 전","ok")
        for (i in feedsData.indices) {
            Log.d("for문","실행")
            val feed = feedsData[i]
            Log.d("feed",feed.toString())
            // user
            val userProfileData = feed.userProfile
            val userName = userProfileData.nickName
            val userImg = userProfileData.thumbnailUrl
            // feed
            val feedImg = feed.thumbnailUrl
            val feedContent = feed.content
            val heartCount = feed.likeCount
            val time = 11
            val commentCount = 10

            feedList.add(CommunityPostItem(userImg,userName,time,feedImg,feedContent,heartCount,commentCount))
        }
        Log.d("for문 후","ok")
        // 리사이클러뷰 어댑터 연결
        rvCommunity.adapter = communityAdapter
        rvCommunity.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        // 어댑터에 데이터가 변경되었음을 알리기
        communityAdapter.notifyDataSetChanged()
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