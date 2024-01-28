package com.example.teuniverse

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHost
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.teuniverse.databinding.FragmentCommunityDetailBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CommunityDetailFragment : Fragment() {

    private lateinit var binding: FragmentCommunityDetailBinding
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var commentList: ArrayList<CommentItem>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCommunityDetailBinding.inflate(inflater, container, false)
        binding.backBtnDetail.setOnClickListener {
            NavHostFragment.findNavController(this).navigate(R.id.action_navigation_communityDetail_to_navigation_community)
        }
        commentList = ArrayList()
        // getArguments()를 통해 Bundle을 받아옴
        val bundle = arguments
        Log.d("bundle",bundle.toString())
        if (bundle != null) {
            val feedId = bundle.getString("feedId")
            Log.d("feedID", feedId.toString())
            binding.detailFeedId.text = feedId.toString()
            lifecycleScope.launch {
                detailFeedApi(feedId.toString())
            }
        } else {
            Log.d("feedId","null")
        }

        // 댓글 리사이클러뷰 어댑터 연결
        commentAdapter = CommentAdapter(commentList)
        binding.rvComment.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.rvComment.adapter = commentAdapter

        return binding.root
    }

    private suspend fun detailFeedApi(feedId: String) {
        Log.d("detailFeedsApi 함수", "호출 성공")
        val accessToken = getAccessToken()
        try {
            if (accessToken != null) {
                val response: Response<ServerResponse<CommunityDetailData>> = withContext(Dispatchers.IO) {
                    CommunityDetailInstance.communityDetailService().getDetailFeeds(feedId, accessToken)
                }
                if (response.isSuccessful) {
                    val theDetailFeed: ServerResponse<CommunityDetailData>? = response.body()
                    if (theDetailFeed != null) {
                        Log.d("detailFeedApi 함수 response", "${theDetailFeed.statusCode} ${theDetailFeed.message}")
                        handleResponse(theDetailFeed)
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

    private fun handleResponse(detailFeedData: ServerResponse<CommunityDetailData>) {
        val detailData = detailFeedData.data
        val commentData = detailData.comments
        // user
        // 프로필 img
        Glide.with(this)
            .load(detailData.userProfile.thumbnailUrl)
            .apply(RequestOptions.circleCropTransform()) // 이미지뷰 모양에 맞추기
            .into(binding.userProfile)

        binding.fandomName.text = detailData.userProfile.nickName // 닉네임

        // feed
        Glide.with(this)
            .load(detailData.thumbnailUrl)
            .apply(RequestOptions.circleCropTransform()) // 이미지뷰 모양에 맞추기
            .into(binding.postImg)

        binding.postContent.text = detailData.content
        binding.heartCount.text = detailData.likeCount.toString()

        for (i in commentData.indices) {
            val comment = commentData[i]
            // comment 리사이클러뷰 리스트 담기
            val userImg = comment.userProfile.thumbnailUrl
            val nickname = comment.userProfile.nickName
            val content = comment.content
            val time = "7분 전"
            commentList.add(CommentItem(userImg, nickname, time, content))
        }
        // 어댑터에 데이터가 변경되었음을 알리기
        commentAdapter.notifyDataSetChanged()
    }

    private fun handleError(errorMessage: String) {
        // 에러를 처리하는 코드
        Log.d("communityFeedsApi 함수 Error", errorMessage)
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
}