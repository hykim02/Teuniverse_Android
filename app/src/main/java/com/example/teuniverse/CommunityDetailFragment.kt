package com.example.teuniverse

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.teuniverse.databinding.FragmentCommunityDetailBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.File

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

        val feedId = getFeedId()
        binding.feedID.text = feedId
        binding.feedID.visibility = GONE

        // 상세 피드 불러오기
        lifecycleScope.launch {
            detailFeedApi(feedId.toString())
        }

        initHeartState() // 하트 상태 초기화
        createCommentBtn() // 댓글 생성

        // 하트 클릭이벤트
        binding.like.setOnClickListener {
            setHeartState()
        }

        // 댓글 리사이클러뷰 어댑터 연결
        commentAdapter = CommentAdapter(commentList, viewLifecycleOwner )

        return binding.root
    }

    // getArguments()를 통해 Bundle을 받아옴
    private fun getFeedId(): String? {
        val bundle = arguments
        Log.d("bundle", bundle.toString())
        return bundle?.getString("feedId")
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
                        detailHandleResponse(theDetailFeed)
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

    private fun detailHandleResponse(detailFeedData: ServerResponse<CommunityDetailData>) {
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
            val commentId = comment.id
            commentList.add(CommentItem(userImg, nickname, time, content, commentId))
        }
        // 리사이클러뷰 어댑터 연결(아이템 개수만큼 생성)
        val spanCount = commentList.size
        Log.d("댓글 개수", spanCount.toString())
        val layoutManager = GridLayoutManager(context, spanCount, GridLayoutManager.HORIZONTAL, false)
        binding.rvComment.adapter = commentAdapter
        binding.rvComment.layoutManager = layoutManager

        // 어댑터에 데이터가 변경되었음을 알리기
        commentAdapter.notifyDataSetChanged()
    }

    // 댓글 생성 api
    private suspend fun createCommentApi(feedId: String, content: String) {
        Log.d("createCommentApi 함수", "호출 성공")
        val accessToken = getAccessToken()
        val content = CreateComment(content = content)
        try {
            if (accessToken != null) {
                val response: Response<SignUpResponse> = withContext(Dispatchers.IO) {
                    CreateCommentInstance.createCommentService().createComment(feedId, accessToken, content)
                }
                if (response.isSuccessful) {
                    val theComment: SignUpResponse? = response.body()
                    if (theComment != null) {
                        Log.d("createCommentApi 함수 response", "${theComment.statusCode} ${theComment.message}")
                    } else {
                        handleError("Response body is null.")
                    }
                } else {
                    handleError("createCommentApi 함수 Error: ${response.code()} - ${response.message()}")
                }
            }
        }
        catch (e: Exception) {
            handleError(e.message ?: "Unknown error occurred.")
        }
    }

    // 댓글 등록 버튼 이벤트
    private fun createCommentBtn() {
        binding.btnEnroll.setOnClickListener {
            val feedId = getFeedId()
            val content = binding.commentTxt.text.toString()
            if (feedId != null) {
                lifecycleScope.launch {
                    createCommentApi(feedId, content)
                }
            }
            findNavController().navigate(R.id.action_navigation_communityDetail_to_navigation_community)
        }
    }

    private fun handleError(errorMessage: String) {
        // 에러를 처리하는 코드
        Log.d("Api 함수 Error", errorMessage)
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

    // 좋아요 생성
    private suspend fun clickLikeApi(feedId: String) {
        Log.d("clickLikeApi 함수", "호출 성공")
        val accessToken = getAccessToken()
        try {
            if (accessToken != null) {
                val response: Response<ServerResponse<CreateHeart>> = withContext(
                    Dispatchers.IO) {
                    ClickLikeInstance.clickLikeService().clickLike(feedId.toInt(), accessToken)
                }
                if (response.isSuccessful) {
                    val theLike: ServerResponse<CreateHeart>? = response.body()
                    if (theLike != null) {
                        Log.d("clickLikeApi 함수 response", "${theLike.statusCode} ${theLike.message}")
                        likeHandleResponse(theLike)
                    } else {
                        handleError("Response body is null.")
                    }
                } else {
                    handleError("clickLikeApi 함수 Error: ${response.code()} - ${response.message()}")
                }
            }
        }
        catch (e: Exception) {
            handleError(e.message ?: "Unknown error occurred.")
        }
    }

    // 좋아요 취소
    private suspend fun cancelClickLikeApi(feedId: String) {
        Log.d("cancelClickLikeApi 함수", "호출 성공")
        val accessToken = getAccessToken()
        try {
            if (accessToken != null) {
                val response: Response<ServerResponse<CreateHeart>> = withContext(
                    Dispatchers.IO) {
                    CancelClickLikeInstance.cancelClickLikeService().cancelClickLike(feedId.toInt(), accessToken)
                }
                if (response.isSuccessful) {
                    val theLike: ServerResponse<CreateHeart>? = response.body()
                    if (theLike != null) {
                        Log.d("cancelClickLikeApi 함수 response", "${theLike.statusCode} ${theLike.message}")
                        likeHandleResponse(theLike)
                    } else {
                        handleError("Response body is null.")
                    }
                } else {
                    handleError("cancelClickLikeApi 함수 Error: ${response.code()} - ${response.message()}")
                }
            }
        }
        catch (e: Exception) {
            handleError(e.message ?: "Unknown error occurred.")
        }
    }

    private fun likeHandleResponse(theLike: ServerResponse<CreateHeart>) {
        binding.heartCount.text = theLike.data.likeCount.toString()
    }

    // 하트 상태 설정
    private fun setHeartState() {
        Log.d("setHeartState 함수", "실행")
        HeartStateDB.init(requireContext())
        val editor = HeartStateDB.getInstance().edit()
        val getData = HeartStateDB.getInstance().all
        val sharedPrefsFile = File("${context?.filesDir?.parent}/shared_prefs/HeartState.xml")
        val isExist = sharedPrefsFile.exists()
        val feedId = binding.feedID.text

        // heart 파일이 존재한다면
        if (isExist) {
            Log.d("heartDB","exist")

            if (getData.containsKey("$feedId")) { // 해당 키가 존재한다면
                Log.d("heartDB","contains key")
                val heartState = getData.getValue("$feedId") // feedID에 해당하는 하트 상태 값

                if (heartState == true) { // 하트가 이미 클릭된 상태
                    binding.like.setImageResource(R.drawable.icon_heart_off)
                    lifecycleScope.launch {
                        cancelClickLikeApi(feedId.toString())
                    }
                    Log.d("heart","true")
                    editor.putBoolean("$feedId", false)
                    editor.apply()
                } else {
                    binding.like.setImageResource(R.drawable.icon_heart_on)
                    lifecycleScope.launch {
                        clickLikeApi(feedId.toString())
                    }
                    Log.d("heart","false")
                    editor.putBoolean("$feedId", true)
                    editor.apply()
                }
            } else { // 파일은 존재하지만 해당 키가 존재하지 않는다면
                Log.d("heartDB","dosen't contains key")
                binding.like.setImageResource(R.drawable.icon_heart_on)
                lifecycleScope.launch {
                    clickLikeApi(feedId.toString())
                }
                Log.d("heartDB","dosen't exist")
                editor.putBoolean("$feedId", true)
                editor.apply()
            }
        } else {  // 존재하지 않는다면 (최초 클릭)
            binding.like.setImageResource(R.drawable.icon_heart_on)
            lifecycleScope.launch {
                clickLikeApi(feedId.toString())
            }
            Log.d("heartDB","dosen't exist")
            editor.putBoolean("$feedId", true)
            editor.apply()
        }
    }

    // 하트 상태 초기화
    private fun initHeartState() {
        Log.d("initHeartState 함수","실행")
        HeartStateDB.init(requireContext())
        val getData = HeartStateDB.getInstance().all
        val sharedPrefsFile = File("${context?.filesDir?.parent}/shared_prefs/HeartState.xml")
        val isExist = sharedPrefsFile.exists()
        val feedId = binding.feedID.text

        if (isExist) {
            Log.d("heartDB","exist")
            for ((key, value) in getData.entries) {
                if (key == feedId.toString()) {
                    val isLiked = value as? Boolean ?: false

                    if (isLiked) {
                        binding.like.setImageResource(R.drawable.icon_heart_on)
                    } else {
                        binding.like.setImageResource(R.drawable.icon_heart_off)
                    }
                }
            }
        }
    }
}