package com.example.teuniverse

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 코루틴을 사용하여 getArtistList 함수 호출
        lifecycleScope.launch {
            communityFeedsApi()
            getNumberOfVotes()
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
        communityAdapter = CommunityPostAdapter(feedList)

        // Find the NavHostFragment
        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        // Get the NavController from the NavHostFragment
        val navController = navHostFragment.navController
        // Obtain the NavGraph from the NavController
        val navGraph = navController.navInflater.inflate(R.navigation.navigation_bar)
        // Set the NavController to the View
        view.findNavController().setGraph(navGraph)
        // 리사이클러뷰 어댑터 연결
        rvCommunity.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
//        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
//        val navController = navHostFragment.navController

//        val navHostFragment = childFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
//        val navController = navHostFragment.findNavController()

        communityAdapter.setOnItemClickListener(object: CommunityPostAdapter.OnItemClickListener{
            override fun onItemClick(view: View, position: Int) {
                Log.d("onItemClick 함수","실행")
                Log.d("position",position.toString())

                findNavController().navigate(R.id.action_navigation_community_to_communityDetailFragment)
//                val navController = findNavController()
//                Log.d("navController", navController.toString())
//                navController.navigate(R.id.action_navigation_community_to_communityDetailFragment)
            }
        })
        rvCommunity.adapter = communityAdapter

//        adapter.setOnItemClickListener(object : BookAdapter.OnItemClickListener {
//            override fun onItemClick(book: String) {
//                val bundle = bundleOf("bookTitle" to book)
//                findNavController().navigate(R.id.action_first_to_detail, bundle)
//            }
//        })
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
            val time = "11분 전"
            val commentCount = 10

            feedList.add(
                CommunityPostItem(
                    userImg, userName,time,feedImg,feedContent,heartCount,commentCount))
            Log.d("feedList",feedList.toString())
        }
        Log.d("for문 후","ok")

        // 어댑터에 데이터가 변경되었음을 알리기
        communityAdapter.notifyDataSetChanged()
    }

//    private var context: Context? = null
//
//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        this.context = context
//    }
//
//    override fun onDetach() {
//        super.onDetach()
//        this.context = null
//    }

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
        Log.d("투표권 개수", votes.data.voteCount.toString())
        val voteCount = votes.data.voteCount
        numberOfVote.text = votes.data.voteCount.toString()
    }
}