package com.example.teuniverse

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.concurrent.TimeUnit

class CommunityDetailFragment : Fragment(), CommentAdapter.OnEditClickListener, PopupDelete.PopupDeleteListener {

    private lateinit var binding: FragmentCommunityDetailBinding
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var commentList: ArrayList<CommentItem>

    @RequiresApi(Build.VERSION_CODES.O)
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

        binding.btnOption.setOnClickListener {
            showPopupMenu(requireContext())
        }

        // 댓글 리사이클러뷰 어댑터 연결
        commentAdapter = CommentAdapter(commentList, viewLifecycleOwner, binding.commentCount, this)

        return binding.root
    }

    // getArguments()를 통해 Bundle을 받아옴
    private fun getFeedId(): String? {
        val bundle = arguments
        return bundle?.getString("feedId")
    }

    @RequiresApi(Build.VERSION_CODES.O)
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

    @RequiresApi(Build.VERSION_CODES.O)
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
        if (detailData.thumbnailUrl != null) {
            Glide.with(this)
                .load(detailData.thumbnailUrl)
                .into(binding.postImg)
        } else {
            binding.postImg.visibility = GONE
        }
        val param = dateTimeToMillSec(detailData.createdAt)
        binding.term.text = calculationTime(param)
        binding.postContent.text = detailData.content
        binding.heartCount.text = detailData.likeCount.toString()
        binding.commentCount.text = detailData.commentCount.toString()

        for (i in commentData.indices) {
            val comment = commentData[i]
            // comment 리사이클러뷰 리스트 담기
            val userImg = comment.userProfile.thumbnailUrl
            val nickname = comment.userProfile.nickName
            val content = comment.content
            val param = dateTimeToMillSec(comment.createdAt)
            val time = calculationTime(param)
            val commentId = comment.id
            commentList.add(CommentItem(userImg, nickname, time, content, commentId))
        }
        // 리사이클러뷰 어댑터 연결(아이템 개수만큼 생성)
        val spanCount = commentList.size
        val layoutManager = GridLayoutManager(context, spanCount, GridLayoutManager.HORIZONTAL, false)
        binding.rvComment.adapter = commentAdapter
        binding.rvComment.layoutManager = layoutManager

        // 어댑터에 데이터가 변경되었음을 알리기
        commentAdapter.notifyDataSetChanged()
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

    // 댓글 생성 api
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun createCommentApi(feedId: String, content: String) {
        Log.d("createCommentApi 함수", "호출 성공")
        val accessToken = getAccessToken()
        val content = CreateComment(content = content)
        try {
            if (accessToken != null) {
                val response: Response<ServerResponse<CreateCommentResponse>> = withContext(Dispatchers.IO) {
                    CreateCommentInstance.createCommentService().createComment(feedId, accessToken, content)
                }
                if (response.isSuccessful) {
                    val theComment: ServerResponse<CreateCommentResponse>? = response.body()
                    if (theComment != null) {
                        Log.d("createCommentApi 함수 response", "${theComment.statusCode} ${theComment.message}")
                        handleResponse(theComment)
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
        binding.commentTxt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // 텍스트 변경 전에 호출되는 메서드
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 텍스트가 변경될 때 호출되는 메서드
                val currentText = s.toString()
                if (currentText.length >= 10) {
                    binding.btnEnroll.setBackgroundResource(R.drawable.enroll_button_event)
                } else {
                    binding.btnEnroll.setBackgroundResource(R.drawable.custom_comment_enroll)
                }
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun afterTextChanged(s: Editable?) {
                VoteMissionDB.init(requireContext())
                val db = VoteMissionDB.getInstance()
                val count = db.getInt("comment", 0)

                // 텍스트 변경 후에 호출되는 메서드
                binding.btnEnroll.setOnClickListener {
                    val feedId = getFeedId()
                    val content = binding.commentTxt.text.toString()
                    if (content.length < 10 && view != null) {
                        Toast.makeText(view!!.context,"10글자 이상 작성 해주세요.", Toast.LENGTH_SHORT).show()
                    } else {
                        if (feedId != null) {
                            lifecycleScope.launch {
                                createCommentApi(feedId, content)

                                if(count <= 4) {
                                    handleMission()
                                    Toast.makeText(requireContext(), "일일미션 댓글쓰기 완료(${count+1}회)", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                        binding.commentTxt.setText("")
                    }
                }
            }
        })
    }

    // 댓글 생성
    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleResponse(theComment: ServerResponse<CreateCommentResponse>) {
        UserInfoDB.init(requireContext())
        val getData = UserInfoDB.getInstance().all
        val response = theComment.data
        binding.commentCount.text = response.commentCount.toString()

        val userImg: String
        val nickname = getData.getValue("nickName").toString()
        val content = response.comment.content
        val commentId = response.comment.id
        val param = dateTimeToMillSec(response.comment.createdAt)
        val time = calculationTime(param)

        if(getData.containsKey("thumbnailUrl")) {
            userImg = getData.getValue("thumbnailUrl").toString()
        } else {
            userImg = getData.getValue("imageFile").toString()
        }

        commentList.add(CommentItem(userImg, nickname, time, content, commentId))

        // 리사이클러뷰 어댑터 연결(아이템 개수만큼 생성)
        val spanCount = commentList.size
        val layoutManager = GridLayoutManager(context, spanCount, GridLayoutManager.HORIZONTAL, false)
        binding.rvComment.adapter = commentAdapter
        binding.rvComment.layoutManager = layoutManager

        // 어댑터에 데이터가 변경되었음을 알리기
        commentAdapter.notifyDataSetChanged()
    }

    private fun handleError(errorMessage: String) {
        // 에러를 처리하는 코드
        Log.d("Api 함수 Error", errorMessage)
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
        HeartStateDB.init(requireContext())
        val editor = HeartStateDB.getInstance().edit()
        val getData = HeartStateDB.getInstance().all
        val sharedPrefsFile = File("${context?.filesDir?.parent}/shared_prefs/HeartState.xml")
        val isExist = sharedPrefsFile.exists()
        val feedId = binding.feedID.text

        // heart 파일이 존재한다면
        if (isExist) {
            if (getData.containsKey("$feedId")) { // 해당 키가 존재한다면
                val heartState = getData.getValue("$feedId") // feedID에 해당하는 하트 상태 값

                if (heartState == true) { // 하트가 이미 클릭된 상태
                    binding.like.setImageResource(R.drawable.icon_heart_off)
                    lifecycleScope.launch {
                        cancelClickLikeApi(feedId.toString())
                    }
                    editor.remove("$feedId")
                    Log.d("하트 취소한 feedID", feedId.toString())
                    editor.apply()
                } else {
                    binding.like.setImageResource(R.drawable.icon_heart_on)
                    lifecycleScope.launch {
                        clickLikeApi(feedId.toString())
                    }
                    editor.putBoolean("$feedId", true)
                    editor.apply()
                }
            } else { // 파일은 존재하지만 해당 키가 존재하지 않는다면
                binding.like.setImageResource(R.drawable.icon_heart_on)
                lifecycleScope.launch {
                    clickLikeApi(feedId.toString())
                }
                editor.putBoolean("$feedId", true)
                editor.apply()
            }
        } else {  // 존재하지 않는다면 (최초 클릭)
            binding.like.setImageResource(R.drawable.icon_heart_on)
            lifecycleScope.launch {
                clickLikeApi(feedId.toString())
            }
            editor.putBoolean("$feedId", true)
            editor.apply()
        }
    }

    // 하트 상태 초기화
    private fun initHeartState() {
        HeartStateDB.init(requireContext())
        val getData = HeartStateDB.getInstance().all
        val sharedPrefsFile = File("${context?.filesDir?.parent}/shared_prefs/HeartState.xml")
        val isExist = sharedPrefsFile.exists()
        val feedId = binding.feedID.text

        if (isExist) {
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

    private fun handleMission() {
        VoteMissionDB.init(requireContext())
        val db = VoteMissionDB.getInstance()
        val editor = db.edit()
        var count = db.getInt("comment", 0)
        count += 1
        editor.putInt("comment", count)
        editor.apply()
    }

    // 피드 삭제 및 수정 옵션
    private fun showPopupMenu(context: Context) {
        val popupMenu = PopupMenu(context, binding.btnOption)
        popupMenu.inflate(R.menu.option_menu)

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.delete -> {
                    // 삭제 버튼 클릭 시 처리
                    Log.d("삭제할 feedID", binding.feedID.text.toString())
                    showPopupDeleteDialog(binding.feedID.text.toString())
                    true
                }
                R.id.edit -> {
                    // 수정 버튼 클릭 시 처리
                    editEvent(context)
                    Log.d("수정할 피드id", binding.feedID.text.toString())
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    // 댓글 수정
    private fun editEvent(context: Context) {
        val intent = Intent(context, CommunityEditActivity::class.java)
        val bundleAdapter = arguments
        val bundle = Bundle().apply {
            putInt("feedId", binding.feedID.text.toString().toInt())
            putString("postSummary", binding.postContent.text.toString())

            if (bundleAdapter != null) {
                val postImg = bundleAdapter.getString("postImg")
                putString("postImg", postImg)
            } else {
                Log.d("bundleAdapter", "null")
            }
        }
        intent.putExtras(bundle)
        context.startActivity(intent)
    }

    // 댓글 수정 버튼 클릭 시 호출되는 메서드
    override fun onEditClick(comment: String, commentId: Int) {
        // 댓글 수정 버튼이 클릭되었을 때 처리할 로직을 여기에 추가
        binding.commentTxt.setText(comment)
        binding.commentTxt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // 텍스트 변경 전에 호출되는 메서드
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 텍스트가 변경될 때 호출되는 메서드
                val currentText = s.toString()
                if (currentText.length >= 10) {
                    binding.btnEnroll.setBackgroundResource(R.drawable.enroll_button_event)
                } else {
                    binding.btnEnroll.setBackgroundResource(R.drawable.custom_comment_enroll)
                }
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun afterTextChanged(s: Editable?) {
                // 텍스트 변경 후에 호출되는 메서드
                binding.btnEnroll.setOnClickListener {
                    val id = commentId
                    val content = binding.commentTxt.text.toString()
                    if (content.length < 10 && view != null) {
                        Toast.makeText(view!!.context,"10글자 이상 작성 해주세요.", Toast.LENGTH_SHORT).show()
                    } else {
                        if (id != null) {
                            lifecycleScope.launch {
                                commentAdapter.editCommentApi(id, requireView(), content)
                            }
                        }
                        binding.commentTxt.setText("")
                    }
                }
            }
        })
    }

    private fun showPopupDeleteDialog(feedId: String) {
        val popupDelete = PopupDelete(requireContext(),feedId, this@CommunityDetailFragment)

        popupDelete.show()

        popupDelete.setOnDismissListener {
            Log.d("popupDelete", "PopupVote dialog dismissed.")
        }
    }

    override fun deleteFeed(feedId: Int) {
        findNavController().navigate(R.id.action_navigation_communityDetail_to_navigation_community)
    }
}