package com.example.teuniverse

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
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

    private fun handleGetFeeds(theFeeds: ServerResponse<CommunityData>) {
        Log.d("handleGetFeeds함수","호출 성공")
        val artistProfileData = theFeeds.data.artistProfile
        val feedsData = theFeeds.data.feeds

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
            val time = "11분 전"
            val commentCount = communityAdapter.itemCount

            feedList.add(
                CommunityPostItem(
                     feedId, userImg, userName,time,feedImg,feedContent,heartCount,commentCount))
        }
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

    //요약 보여주기 - 공지 전체내용 중 일부(15자)만 가져옴
    //15자 이상인 경우 일부를 자르고 "..."을 붙임
    private fun getContextPreview(context: String): String {
        return if (context.isNotEmpty()) {
            val trimmedText = if (context.length >= 85) "${context.substring(0, 85)}..." else context
            trimmedText
        } else {
            ""
        }
    }
}


